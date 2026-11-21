package com.assetpilot.warranty.service.impl;

import com.assetpilot.warranty.exception.InvalidContractException;
import com.assetpilot.warranty.exception.WarrantyNotFoundException;
import com.assetpilot.warranty.model.WarrantyEntity;
import com.assetpilot.warranty.repository.WarrantyRepository;
import com.assetpilot.warranty.service.WarrantyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WarrantyServiceImpl implements WarrantyService {

    private final WarrantyRepository warrantyRepository;

    public WarrantyServiceImpl(WarrantyRepository warrantyRepository) {
        this.warrantyRepository = warrantyRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WarrantyEntity getWarrantyByAssetId(String assetId) {
        if (assetId == null || assetId.trim().isEmpty()) {
            throw new InvalidContractException("L'identifiant de l'équipement (assetId) est obligatoire.");
        }
        return warrantyRepository.findByAssetId(assetId.trim())
                .orElseThrow(() -> new WarrantyNotFoundException(assetId.trim()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWarrantyCovered(String assetId) {
        WarrantyEntity entity = getWarrantyByAssetId(assetId);
        return entity.isCovered();
    }

    @Override
    public WarrantyEntity saveWarranty(WarrantyEntity warranty) {
        if (warranty.getAssetId() == null || warranty.getAssetId().trim().isEmpty()) {
            throw new InvalidContractException("L'identifiant de l'équipement (assetId) ne peut pas être vide.");
        }
        if (warranty.getContractNumber() == null || warranty.getContractNumber().trim().isEmpty()) {
            throw new InvalidContractException("Le numéro de contrat (contractNumber) ne peut pas être vide.");
        }
        return warrantyRepository.save(warranty);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarrantyEntity> getAllWarranties() {
        return warrantyRepository.findAll();
    }
}
