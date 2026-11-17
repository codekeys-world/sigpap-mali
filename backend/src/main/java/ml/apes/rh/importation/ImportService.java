package ml.apes.rh.importation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.catalog.ArreteIntegrationRepository;
import ml.apes.rh.catalog.CorpsRepository;
import ml.apes.rh.catalog.GradeRepository;
import ml.apes.rh.catalog.ServiceApRepository;
import ml.apes.rh.domain.*;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.importation.Dtos.*;
import ml.apes.rh.personnel.PersonnelRepository;
import ml.apes.rh.personnel.PersonnelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Import Excel des agents. Le fichier est d'abord vérifié ligne par ligne (mode simulation), puis importé :
 * les lignes valides sont créées, les autres sont listées avec leur message d'erreur.
 */
@Service
@RequiredArgsConstructor
public class ImportService {

    private static final int MAX_LIGNES = 2000;
    private static final String[] COLONNES = {
        "Matricule",
        "Prénom",
        "Nom",
        "Sexe",
        "Date de naissance",
        "Lieu de naissance",
        "Téléphone",
        "E-mail",
        "Adresse",
        "Date de recrutement",
        "Date de titularisation",
        "Grade",
        "Corps",
        "Service",
        "Arrêté d'intégration",
        "Surveillant",
        "Mot de passe",
    };
    private static final Pattern MATRICULE = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Pattern TEL = Pattern.compile("^[+0-9 ().-]{6,20}$");
    private static final List<DateTimeFormatter> FORMATS = List.of(
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("d-M-yyyy"),
        DateTimeFormatter.ofPattern("d.M.yyyy")
    );
    private static final String ALPHABET = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";

    private final PersonnelService personnelService;
    private final PersonnelRepository repo;
    private final GradeRepository gradeRepo;
    private final CorpsRepository corpsRepo;
    private final ServiceApRepository serviceRepo;
    private final ArreteIntegrationRepository arreteIntegrationRepo;
    private final SecureRandom random = new SecureRandom();

    private static String norm(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase()
            .replaceAll("[^a-z0-9]", "");
    }

    // ------------------------------------------------------------------ modèle

    public byte[] modele(
        List<Grade> grades,
        List<ServiceAp> services,
        List<Corps> corps,
        List<ArreteIntegration> arretes
    ) {
        try (
            XSSFWorkbook wb = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            XSSFCellStyle head = wb.createCellStyle();
            head.setFillForegroundColor(new XSSFColor(new byte[] { 75, 13, 13 }, null));
            head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            XSSFFont font = wb.createFont();
            font.setBold(true);
            font.setColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }, null));
            head.setFont(font);

            XSSFSheet sheet = wb.createSheet("Agents");
            Row h = sheet.createRow(0);
            for (int i = 0; i < COLONNES.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(COLONNES[i] + (i < 4 || i == 14 ? " *" : ""));
                c.setCellStyle(head);
                sheet.setColumnWidth(i, 22 * 256);
            }
            sheet.createFreezePane(0, 1);
            XSSFDataValidationHelper dv = new XSSFDataValidationHelper(sheet);
            addList(sheet, dv, new String[] { "M", "F" }, 3);
            addList(sheet, dv, new String[] { "Oui", "Non" }, 15);

            XSSFSheet info = wb.createSheet("Consignes");
            String[] lignes = {
                "SIGPAP-MALI — IMPORT DES AGENTS — CONSIGNES",
                "1. Remplissez l'onglet « Agents » : une ligne par agent. Les colonnes marquées * sont obligatoires.",
                "2. Matricule : 3 à 30 caractères (lettres, chiffres, . _ -). Il doit être unique.",
                "3. Sexe : M ou F. Dates : JJ/MM/AAAA (ou cellule de type date Excel).",
                "4. Pour un surveillant, renseignez le grade. Pour un non surveillant, laissez le grade vide et indiquez son corps.",
                "5. Grade, corps, service et arrêté d'intégration doivent correspondre aux valeurs de l'onglet « Listes ».",
                "6. L'arrêté d'intégration est obligatoire pour chaque agent.",
                "7. Surveillant : Oui ou Non (Oui par défaut).",
                "8. Mot de passe : facultatif (8 caractères minimum). S'il est vide, un mot de passe temporaire est généré ;",
                "   l'agent devra le changer à sa première connexion.",
                "9. Dans l'application : Personnel > Importer > « Vérifier le fichier » (aucune donnée n'est enregistrée), puis « Importer ».",
                "10. Les lignes en erreur sont ignorées et listées avec leur motif ; corrigez-les puis réimportez uniquement ces lignes.",
                "11. Maximum " + MAX_LIGNES + " lignes par fichier.",
            };
            for (int i = 0; i < lignes.length; i++) info.createRow(i)
                .createCell(0)
                .setCellValue(lignes[i]);
            info.setColumnWidth(0, 130 * 256);

            XSSFSheet listes = wb.createSheet("Listes");
            Row lh = listes.createRow(0);
            String[] tl = {
                "Grade",
                "Corps du grade",
                "",
                "Service",
                "Type de service",
                "Corps",
                "Arrêté d'intégration",
            };
            for (int i = 0; i < tl.length; i++) {
                Cell c = lh.createCell(i);
                c.setCellValue(tl[i]);
                c.setCellStyle(head);
                listes.setColumnWidth(i, 36 * 256);
            }
            for (int i = 0; i < grades.size(); i++) {
                Row r =
                    listes.getRow(i + 1) != null ? listes.getRow(i + 1) : listes.createRow(i + 1);
                r.createCell(0).setCellValue(grades.get(i).getNom());
                r.createCell(1).setCellValue(grades.get(i).getCorps().getNom());
            }
            for (int i = 0; i < services.size(); i++) {
                Row r =
                    listes.getRow(i + 1) != null ? listes.getRow(i + 1) : listes.createRow(i + 1);
                r.createCell(3).setCellValue(services.get(i).getNom());
                r.createCell(4).setCellValue(services.get(i).getType().name());
            }
            for (int i = 0; i < corps.size(); i++) {
                Row r =
                    listes.getRow(i + 1) != null ? listes.getRow(i + 1) : listes.createRow(i + 1);
                r.createCell(5).setCellValue(corps.get(i).getNom());
            }
            for (int i = 0; i < arretes.size(); i++) {
                Row r =
                    listes.getRow(i + 1) != null ? listes.getRow(i + 1) : listes.createRow(i + 1);
                r.createCell(6).setCellValue(arretes.get(i).getReference());
            }
            addRangeList(sheet, dv, "Listes!$A$2:$A$2000", 11);
            addRangeList(sheet, dv, "Listes!$F$2:$F$2000", 12);
            addRangeList(sheet, dv, "Listes!$D$2:$D$2000", 13);
            addRangeList(sheet, dv, "Listes!$G$2:$G$2000", 14);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Impossible de générer le modèle Excel.");
        }
    }

    private void addList(XSSFSheet sheet, XSSFDataValidationHelper dv, String[] valeurs, int col) {
        DataValidation v = dv.createValidation(
            dv.createExplicitListConstraint(valeurs),
            new CellRangeAddressList(1, MAX_LIGNES, col, col)
        );
        v.setShowErrorBox(true);
        sheet.addValidationData(v);
    }

    private void addRangeList(XSSFSheet sheet, XSSFDataValidationHelper dv, String range, int col) {
        DataValidation validation = dv.createValidation(
            dv.createFormulaListConstraint(range),
            new CellRangeAddressList(1, MAX_LIGNES, col, col)
        );
        validation.setShowErrorBox(true);
        sheet.addValidationData(validation);
    }

    // ------------------------------------------------------------------ import

    public ImportResult importer(MultipartFile file, boolean simulation) {
        if (file == null || file.isEmpty()) throw new BusinessException("Aucun fichier reçu.");
        String nom =
            file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!nom.endsWith(".xlsx")) throw new BusinessException(
            "Le fichier doit être au format Excel (.xlsx)."
        );

        List<Grade> grades = gradeRepo.findAll();
        List<Corps> corps = corpsRepo.findAll();
        List<ServiceAp> services = serviceRepo.findAll();
        List<ArreteIntegration> arretes = arreteIntegrationRepo.findAll();
        List<ErreurImport> erreurs = new ArrayList<>();
        List<CompteGenere> comptes = new ArrayList<>();
        Set<String> vus = new HashSet<>();
        int total = 0,
            valides = 0,
            crees = 0;

        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheet("Agents") != null ? wb.getSheet("Agents") : wb.getSheetAt(0);
            Map<String, Integer> col = new HashMap<>();
            Row entete = sheet.getRow(0);
            if (entete == null) throw new BusinessException(
                "La première ligne du fichier doit contenir les en-têtes de colonnes."
            );
            for (Cell c : entete) col.put(norm(c.getStringCellValue()), c.getColumnIndex());
            List<String> manquantes = new ArrayList<>();
            for (String r : new String[] {
                "Matricule",
                "Prénom",
                "Nom",
                "Sexe",
                "Arrêté d'intégration",
            }) {
                if (!col.containsKey(norm(r))) manquantes.add(r);
            }
            if (!manquantes.isEmpty()) throw new BusinessException(
                "Colonnes obligatoires manquantes : " +
                    String.join(", ", manquantes) +
                    ". Utilisez le modèle."
            );

            DataFormatter fmt = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || vide(row, fmt)) continue;
                if (++total > MAX_LIGNES) throw new BusinessException(
                    "Le fichier dépasse " + MAX_LIGNES + " lignes."
                );
                List<String> errs = new ArrayList<>();
                Personnel p = new Personnel();
                String matricule = txt(row, col.get("matricule"), fmt);
                if (matricule.isEmpty()) errs.add("Matricule obligatoire");
                else if (!MATRICULE.matcher(matricule).matches()) errs.add(
                    "Matricule invalide (3 à 30 caractères : lettres, chiffres, . _ -)"
                );
                else if (!vus.add(matricule.toLowerCase())) errs.add(
                    "Matricule en double dans le fichier"
                );
                else if (repo.existsByMatricule(matricule)) errs.add(
                    "Matricule déjà existant dans l'application"
                );
                p.setMatricule(matricule);

                p.setNom(txt(row, col.get("nom"), fmt));
                p.setPrenom(txt(row, col.get("prenom"), fmt));
                if (p.getNom().length() < 2) errs.add("Nom obligatoire (2 caractères minimum)");
                if (p.getPrenom().length() < 2) errs.add(
                    "Prénom obligatoire (2 caractères minimum)"
                );
                String sx = norm(txt(row, col.get("sexe"), fmt));
                switch (sx) {
                    case "m", "h", "masculin", "homme" -> p.setSexe(Sexe.M);
                    case "f", "feminin", "femme" -> p.setSexe(Sexe.F);
                    default -> errs.add("Sexe invalide (M ou F)");
                }
                LocalDate naissance = date(
                    row,
                    col.get("datedenaissance"),
                    fmt,
                    "Date de naissance",
                    errs
                );
                if (
                    naissance != null &&
                    (naissance.isAfter(LocalDate.now()) ||
                        naissance.isBefore(LocalDate.of(1930, 1, 1)))
                ) errs.add("Date de naissance invalide");
                p.setDateNaissance(naissance);
                LocalDate recrutement = date(
                    row,
                    col.get("datederecrutement"),
                    fmt,
                    "Date de recrutement",
                    errs
                );
                if (recrutement != null && recrutement.isAfter(LocalDate.now())) errs.add(
                    "Date de recrutement dans le futur"
                );
                if (
                    recrutement != null && naissance != null && recrutement.isBefore(naissance)
                ) errs.add("Recrutement antérieur à la naissance");
                p.setDateRecrutement(recrutement);
                LocalDate titularisation = date(
                    row,
                    col.get("datedetitularisation"),
                    fmt,
                    "Date de titularisation",
                    errs
                );
                if (titularisation != null && titularisation.isAfter(LocalDate.now())) errs.add(
                    "Date de titularisation dans le futur"
                );
                if (
                    titularisation != null &&
                    recrutement != null &&
                    titularisation.isBefore(recrutement)
                ) errs.add("Titularisation anterieure au recrutement");
                if (
                    titularisation != null &&
                    naissance != null &&
                    titularisation.isBefore(naissance)
                ) errs.add("Titularisation anterieure a la naissance");
                p.setDateTitularisation(titularisation);
                p.setLieuNaissance(txt(row, col.get("lieudenaissance"), fmt));
                String tel = txt(row, col.get("telephone"), fmt);
                if (!tel.isEmpty() && !TEL.matcher(tel).matches()) errs.add("Téléphone invalide");
                p.setTelephone(tel.isEmpty() ? null : tel);
                String mail = txt(row, col.get("email"), fmt);
                if (!mail.isEmpty() && !EMAIL.matcher(mail).matches()) errs.add("E-mail invalide");
                p.setEmail(mail.isEmpty() ? null : mail);
                p.setAdresse(txt(row, col.get("adresse"), fmt));
                String surv = norm(txt(row, col.get("surveillant"), fmt));
                boolean surveillant = !(
                    surv.equals("non") ||
                    surv.equals("n") ||
                    surv.equals("false") ||
                    surv.equals("0")
                );
                p.setSurveillant(surveillant);

                // grade (et corps pour lever l'ambiguïté) et service
                String gNom = txt(row, col.get("grade"), fmt),
                    cNom = txt(row, col.get("corps"), fmt);
                if (!surveillant && !gNom.isEmpty()) errs.add(
                    "Un agent non surveillant ne porte pas de grade"
                );
                if (!gNom.isEmpty() && surveillant) {
                    List<Grade> cands = grades
                        .stream()
                        .filter(
                            g ->
                                norm(g.getNom()).equals(norm(gNom)) &&
                                (cNom.isEmpty() || norm(g.getCorps().getNom()).equals(norm(cNom)))
                        )
                        .toList();
                    if (cands.isEmpty()) errs.add(
                        "Grade « " +
                            gNom +
                            " » introuvable" +
                            (cNom.isEmpty() ? "" : " dans le corps « " + cNom + " »")
                    );
                    else if (cands.size() > 1) errs.add(
                        "Grade « " +
                            gNom +
                            " » présent dans plusieurs corps : renseignez la colonne Corps"
                    );
                    else {
                        p.setGradeActuel(cands.get(0));
                        p.setCorpsActuel(cands.get(0).getCorps());
                    }
                }
                if (!cNom.isEmpty()) {
                    Corps corpsChoisi = corps
                        .stream()
                        .filter(c -> norm(c.getNom()).equals(norm(cNom)))
                        .findFirst()
                        .orElse(null);
                    if (corpsChoisi == null) errs.add("Corps « " + cNom + " » introuvable");
                    else if (
                        p.getGradeActuel() != null &&
                        !p.getGradeActuel().getCorps().getId().equals(corpsChoisi.getId())
                    ) {
                        errs.add("Le corps choisi ne correspond pas au grade");
                    } else p.setCorpsActuel(corpsChoisi);
                } else if (!surveillant) errs.add(
                    "Le corps est obligatoire pour un agent non surveillant"
                );
                String sNom = txt(row, col.get("service"), fmt);
                if (!sNom.isEmpty()) {
                    List<ServiceAp> cands = services
                        .stream()
                        .filter(s -> norm(s.getNom()).equals(norm(sNom)))
                        .toList();
                    if (cands.isEmpty()) errs.add("Service « " + sNom + " » introuvable");
                    else if (cands.size() > 1) errs.add(
                        "Service « " + sNom + " » ambigu (plusieurs services portent ce nom)"
                    );
                    else p.setServiceCourant(cands.get(0));
                }
                String arreteRef = txt(row, col.get(norm("Arrêté d'intégration")), fmt);
                if (arreteRef.isEmpty()) {
                    errs.add("Arrêté d'intégration obligatoire");
                } else {
                    ArreteIntegration arrete = arretes
                        .stream()
                        .filter(a -> norm(a.getReference()).equals(norm(arreteRef)))
                        .findFirst()
                        .orElse(null);
                    if (arrete == null) errs.add(
                        "Arrêté d'intégration « " + arreteRef + " » introuvable"
                    );
                    else p.setArreteIntegration(arrete);
                }

                String mdp = txt(row, col.get("motdepasse"), fmt);
                boolean genere = mdp.isEmpty();
                if (!genere && mdp.length() < 8) errs.add(
                    "Mot de passe trop court (8 caractères minimum)"
                );
                if (genere) mdp = motDePasseAleatoire();
                p.setMotDePasse(mdp);

                if (!errs.isEmpty()) {
                    erreurs.add(new ErreurImport(i + 1, matricule, String.join(" ; ", errs)));
                    continue;
                }
                valides++;
                if (simulation) continue;
                try {
                    personnelService.creerImporte(p); // transaction propre à chaque agent : une erreur n'annule pas les autres
                    crees++;
                    if (genere) comptes.add(
                        new CompteGenere(matricule, p.getPrenom() + " " + p.getNom(), mdp)
                    );
                } catch (RuntimeException ex) {
                    valides--;
                    erreurs.add(
                        new ErreurImport(
                            i + 1,
                            matricule,
                            ex.getMessage() != null ? ex.getMessage() : "Erreur à l'enregistrement"
                        )
                    );
                }
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof BusinessException be) throw be;
            throw new BusinessException(
                "Fichier Excel illisible : vérifiez qu'il s'agit bien d'un .xlsx valide."
            );
        }
        if (total == 0) throw new BusinessException("Le fichier ne contient aucune ligne d'agent.");
        return new ImportResult(simulation, total, valides, crees, erreurs, comptes);
    }

    // ------------------------------------------------------------------ utilitaires de lecture

    private static boolean vide(Row row, DataFormatter fmt) {
        for (Cell c : row) if (!fmt.formatCellValue(c).trim().isEmpty()) return false;
        return true;
    }

    private static String txt(Row row, Integer col, DataFormatter fmt) {
        if (col == null) return "";
        Cell c = row.getCell(col);
        return c == null ? "" : fmt.formatCellValue(c).trim();
    }

    private static LocalDate date(
        Row row,
        Integer col,
        DataFormatter fmt,
        String libelle,
        List<String> errs
    ) {
        if (col == null) return null;
        Cell c = row.getCell(col);
        if (c == null) return null;
        if (c.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(c)) {
            return c.getLocalDateTimeCellValue().toLocalDate();
        }
        String t = fmt.formatCellValue(c).trim();
        if (t.isEmpty()) return null;
        for (DateTimeFormatter f : FORMATS) {
            try {
                return LocalDate.parse(t, f);
            } catch (DateTimeParseException ignored) {
                /* essai suivant */
            }
        }
        errs.add(libelle + " invalide (« " + t + " » : utilisez JJ/MM/AAAA)");
        return null;
    }

    private String motDePasseAleatoire() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}
