package com.assetpilot.warranty;

import com.assetpilot.warranty.model.WarrantyEntity;
import com.assetpilot.warranty.repository.WarrantyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class WarrantyApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarrantyApplication.class, args);
    }

    /**
     * Initialise un jeu de données de test dans la base H2 dédiée au démarrage du microservice.
     */
    @Bean
    CommandLineRunner initData(WarrantyRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new WarrantyEntity(
                        "WAR-1001",
                        "AST-001",
                        "DELL-PRO-2024-001",
                        "Dell Technologies Partner Care",
                        LocalDate.now().minusMonths(6),
                        LocalDate.now().plusYears(2),
                        WarrantyEntity.WarrantyStatus.ACTIVE,
                        WarrantyEntity.CoverageType.PREMIUM,
                        "24/7 4h On-site Support",
                        "Couverture pièces, main d'œuvre et intervention sur site sous 4 heures.",
                        "support-dell@partner.assetpilot.com"
                ));

                repository.save(new WarrantyEntity(
                        "WAR-1002",
                        "AST-002",
                        "HP-CARE-2022-094",
                        "HP Care Services",
                        LocalDate.now().minusYears(3),
                        LocalDate.now().minusMonths(2),
                        WarrantyEntity.WarrantyStatus.EXPIRED,
                        WarrantyEntity.CoverageType.STANDARD,
                        "Next Business Day",
                        "Garantie standard expirée. Renouvellement possible auprès de HP Care.",
                        "support-hp@partner.assetpilot.com"
                ));

                repository.save(new WarrantyEntity(
                        "WAR-1003",
                        "AST-003",
                        "APPLE-ENT-2023-772",
                        "AppleCare Enterprise",
                        LocalDate.now().minusMonths(10),
                        LocalDate.now().plusMonths(14),
                        WarrantyEntity.WarrantyStatus.ACTIVE,
                        WarrantyEntity.CoverageType.EXTENDED,
                        "Next Business Day On-site",
                        "Couverture mondiale pièces et main d'œuvre avec remplacement prioritaire.",
                        "applecare-enterprise@partner.assetpilot.com"
                ));

                repository.save(new WarrantyEntity(
                        "WAR-1004",
                        "AST-004",
                        "LEN-VOI-2023-012",
                        "Lenovo Premier Support",
                        LocalDate.now().minusYears(1),
                        LocalDate.now().plusYears(1),
                        WarrantyEntity.WarrantyStatus.VOIDED,
                        WarrantyEntity.CoverageType.PARTS_ONLY,
                        "Standard 5x8 Depot",
                        "Garantie annulée suite à modification non autorisée du matériel.",
                        "premier-lenovo@partner.assetpilot.com"
                ));
            }
        };
    }
}
