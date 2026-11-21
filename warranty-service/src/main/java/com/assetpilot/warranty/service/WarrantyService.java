package com.assetpilot.warranty.service;

import com.assetpilot.warranty.model.WarrantyEntity;
import java.util.List;

public interface WarrantyService {

    /**
     * Recherche la garantie associée à un équipement.
     * @param assetId identifiant de l'équipement
     * @return entité WarrantyEntity
     * @throws com.assetpilot.warranty.exception.WarrantyNotFoundException si non trouvée
     * @throws com.assetpilot.warranty.exception.InvalidContractException si assetId vide ou invalide
     */
    WarrantyEntity getWarrantyByAssetId(String assetId);

    /**
     * Vérifie si un équipement dispose d'une garantie active et valide à ce jour.
     * @param assetId identifiant de l'équipement
     * @return true si couverte, false sinon
     */
    boolean isWarrantyCovered(String assetId);

    /**
     * Enregistre ou met à jour une garantie.
     */
    WarrantyEntity saveWarranty(WarrantyEntity warranty);

    /**
     * Liste toutes les garanties existantes.
     */
    List<WarrantyEntity> getAllWarranties();
}
