package ml.apes.rh.personnel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ml.apes.rh.catalog.CorpsRepository;
import ml.apes.rh.catalog.GradeRepository;
import ml.apes.rh.catalog.ServiceApRepository;
import ml.apes.rh.domain.Personnel;
import ml.apes.rh.exception.BusinessException;
import ml.apes.rh.retirement.Dtos.RetraiteLigne;
import ml.apes.rh.retirement.RetraiteService;
import ml.apes.rh.security.CurrentUserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

/** Exports Excel / Word des agents (selon les filtres) et du suivi des retraites. */
@Service
@RequiredArgsConstructor
public class ExportService {

    private static final DateTimeFormatter FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] ENTETES = {
        "Matricule",
        "Prénom",
        "Nom",
        "Sexe",
        "Date de naissance",
        "Âge",
        "Lieu de naissance",
        "Téléphone",
        "E-mail",
        "Corps",
        "Grade",
        "Service",
        "Situation",
        "Depuis le",
        "Motif / cause",
        "Surveillant",
        "En formation",
        "Date de recrutement",
        "Date de titularisation",
        "Retraite prévue le",
    };

    private final RetraiteService retraiteService;
    private final CorpsRepository corpsRepo;
    private final GradeRepository gradeRepo;
    private final ServiceApRepository serviceRepo;
    private final CurrentUserService current;

    private static String oui(boolean b) {
        return b ? "Oui" : "Non";
    }

    private static String s(Object o) {
        return o == null ? "" : o.toString();
    }

    private static String d(LocalDate x) {
        return x == null ? "" : x.format(FR);
    }

    private String[] ligne(Personnel p) {
        var corps =
            p.getCorpsActuel() != null
                ? p.getCorpsActuel()
                : p.getGradeActuel() != null
                  ? p.getGradeActuel().getCorps()
                  : null;
        return new String[] {
            p.getMatricule(),
            p.getPrenom(),
            p.getNom(),
            s(p.getSexe()),
            d(p.getDateNaissance()),
            p.getDateNaissance() == null
                ? ""
                : String.valueOf(Period.between(p.getDateNaissance(), LocalDate.now()).getYears()),
            s(p.getLieuNaissance()),
            s(p.getTelephone()),
            s(p.getEmail()),
            corps != null ? corps.getNom() : "",
            p.getGradeActuel() != null ? p.getGradeActuel().getNom() : "",
            p.getServiceCourant() != null ? p.getServiceCourant().getNom() : "",
            p.getSituation().libelle(),
            d(p.getSituationDepuis()),
            s(p.getSituationMotif()),
            oui(p.isSurveillant()),
            oui(p.isEnFormation()),
            d(p.getDateRecrutement()),
            d(p.getDateTitularisation()),
            d(retraiteService.datePrevue(p)),
        };
    }

    /** Description lisible des filtres appliqués (onglet « Critères » de l'export). */
    public List<String[]> criteres(PersonnelFilter f) {
        List<String[]> c = new ArrayList<>();
        if (f.getQ() != null && !f.getQ().isBlank()) c.add(new String[] { "Recherche", f.getQ() });
        if (f.getCorpsSlug() != null) c.add(new String[] {
            "Corps",
            corpsRepo
                .findBySlug(f.getCorpsSlug())
                .map(x -> x.getNom())
                .orElse("Corps introuvable"),
        });
        if (f.getGradeSlug() != null) c.add(new String[] {
            "Grade",
            gradeRepo
                .findBySlug(f.getGradeSlug())
                .map(x -> x.getNom())
                .orElse("Grade introuvable"),
        });
        if (f.getServiceSlug() != null) c.add(new String[] {
            "Service",
            serviceRepo
                .findBySlug(f.getServiceSlug())
                .map(x -> x.getNom())
                .orElse("Service introuvable"),
        });
        if (f.getStatut() != null && !f.getStatut().isBlank()) {
            String lib = switch (f.getStatut()) {
                case "ACTIF" -> "En activité";
                case "MALADE" -> "Malade";
                case "ABANDON_POSTE" -> "Abandon de poste";
                case "DECEDE" -> "Décédé";
                case "RETRAITE" -> "Retraité";
                default -> f.getStatut();
            };
            c.add(new String[] { "Situation", lib });
        }
        if (Boolean.TRUE.equals(f.getSurveillant())) c.add(new String[] {
            "Surveillants uniquement",
            "Oui",
        });
        if (Boolean.TRUE.equals(f.getEnFormation())) c.add(new String[] { "En formation", "Oui" });
        if (Boolean.TRUE.equals(f.getEnEtude())) c.add(new String[] { "En étude", "Oui" });
        if (Boolean.TRUE.equals(f.getDecede())) c.add(new String[] { "Décédés", "Oui" });
        if (c.isEmpty()) c.add(new String[] {
            "Filtres",
            "Aucun : tous les agents du périmètre de l'utilisateur",
        });
        return c;
    }

    private XSSFCellStyle styleEntete(XSSFWorkbook wb) {
        XSSFCellStyle head = wb.createCellStyle();
        head.setFillForegroundColor(new XSSFColor(new byte[] { 75, 13, 13 }, null));
        head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(new XSSFColor(new byte[] { (byte) 255, (byte) 255, (byte) 255 }, null));
        head.setFont(font);
        return head;
    }

    private void tableau(XSSFWorkbook wb, String nom, String[] entetes, List<String[]> lignes) {
        XSSFSheet sheet = wb.createSheet(nom);
        XSSFCellStyle head = styleEntete(wb);
        Row h = sheet.createRow(0);
        for (int i = 0; i < entetes.length; i++) {
            Cell c = h.createCell(i);
            c.setCellValue(entetes[i]);
            c.setCellStyle(head);
            sheet.setColumnWidth(i, 20 * 256);
        }
        int r = 1;
        for (String[] l : lignes) {
            Row row = sheet.createRow(r++);
            for (int i = 0; i < l.length; i++) row.createCell(i).setCellValue(l[i]);
        }
        sheet.createFreezePane(0, 1);
        if (!lignes.isEmpty()) sheet.setAutoFilter(
            new org.apache.poi.ss.util.CellRangeAddress(0, lignes.size(), 0, entetes.length - 1)
        );
    }

    public byte[] excel(List<Personnel> liste, List<String[]> criteres) {
        try (
            XSSFWorkbook wb = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            tableau(wb, "Personnel", ENTETES, liste.stream().map(this::ligne).toList());
            wb.getProperties().getCoreProperties().setTitle("SIGPAP-MALI - Liste du personnel");
            wb.getProperties().getCoreProperties().setCreator("AP");
            List<String[]> infos = new ArrayList<>();
            infos.add(new String[] { "Application", "SIGPAP-MALI" });
            infos.add(new String[] {
                "Export du",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm")),
            });
            infos.add(new String[] { "Généré par", current.get().getMatricule() });
            infos.add(new String[] { "Nombre d'agents", String.valueOf(liste.size()) });
            infos.addAll(criteres);
            tableau(wb, "Critères", new String[] { "Critère", "Valeur" }, infos);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Impossible de générer le fichier Excel.");
        }
    }

    public byte[] retraites(List<RetraiteLigne> liste) {
        try (
            XSSFWorkbook wb = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            tableau(
                wb,
                "Suivi des retraites",
                new String[] {
                    "Matricule",
                    "Prénom",
                    "Nom",
                    "Grade",
                    "Corps",
                    "Service",
                    "Date de naissance",
                    "Âge",
                    "Âge de retraite",
                    "Retraite prévue le",
                    "Mois restants",
                    "État",
                    "Situation",
                    "Retraité le",
                    "Motif",
                },
                liste
                    .stream()
                    .map(l -> new String[] {
                        l.matricule(),
                        l.prenom(),
                        l.nom(),
                        s(l.grade()),
                        s(l.corps()),
                        s(l.service()),
                        d(l.dateNaissance()),
                        s(l.age()),
                        String.valueOf(l.ageRetraite()),
                        d(l.datePrevue()),
                        s(l.moisRestants()),
                        switch (l.etat()) {
                            case "DEPASSEE" -> "Âge de retraite dépassé";
                            case "A_VENIR" -> "À venir";
                            case "RETRAITE" -> "Retraité";
                            default -> "Date de naissance manquante";
                        },
                        l.situation(),
                        d(l.dateRetraite()),
                        s(l.motifRetraite()),
                    })
                    .toList()
            );
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Impossible de générer le fichier Excel.");
        }
    }

    public byte[] word(List<Personnel> liste) {
        try (
            XWPFDocument doc = new XWPFDocument();
            ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            XWPFRun titre = doc.createParagraph().createRun();
            titre.setBold(true);
            titre.setFontSize(16);
            titre.setColor("4B0D0D");
            titre.setText("SIGPAP-MALI - Liste du personnel (" + liste.size() + ")");
            String[] cols = {
                "Matricule",
                "Prénom et nom",
                "Corps",
                "Grade",
                "Service",
                "Situation",
            };
            XWPFTable table = doc.createTable(liste.size() + 1, cols.length);
            for (int i = 0; i < cols.length; i++) {
                XWPFTableCell c = table.getRow(0).getCell(i);
                c.setColor("4B0D0D");
                XWPFRun run = c.getParagraphArray(0).createRun();
                run.setBold(true);
                run.setColor("FFFFFF");
                run.setText(cols[i]);
            }
            int r = 1;
            for (Personnel p : liste) {
                String statut =
                    p.getSituation().libelle() + (p.isEnFormation() ? " · en formation" : "");
                var corps =
                    p.getCorpsActuel() != null
                        ? p.getCorpsActuel()
                        : p.getGradeActuel() != null
                          ? p.getGradeActuel().getCorps()
                          : null;
                String[] v = {
                    p.getMatricule(),
                    p.getPrenom() + " " + p.getNom(),
                    corps != null ? corps.getNom() : "",
                    p.getGradeActuel() != null ? p.getGradeActuel().getNom() : "",
                    p.getServiceCourant() != null ? p.getServiceCourant().getNom() : "",
                    statut,
                };
                for (int i = 0; i < v.length; i++) table.getRow(r).getCell(i).setText(v[i]);
                r++;
            }
            doc.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Impossible de générer le fichier Word.");
        }
    }
}
