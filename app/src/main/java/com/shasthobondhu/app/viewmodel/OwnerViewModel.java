package com.shasthobondhu.app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.shasthobondhu.app.data.local.entities.HospitalEntity;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import com.shasthobondhu.app.data.local.entities.PharmacyEntity;
import com.shasthobondhu.app.data.repository.HospitalRepository;
import com.shasthobondhu.app.data.repository.MedicineRepository;
import com.shasthobondhu.app.data.repository.PharmacyRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Backs the pharmacy-owner and hospital-owner dashboards. */
@HiltViewModel
public class OwnerViewModel extends ViewModel {

    private final PharmacyRepository pharmacyRepository;
    private final HospitalRepository hospitalRepository;
    private final MedicineRepository medicineRepository;

    private final MediatorLiveData<PharmacyEntity> pharmacy = new MediatorLiveData<>();
    private final MediatorLiveData<HospitalEntity> hospital = new MediatorLiveData<>();
    private final MediatorLiveData<List<MedicineEntity>> medicines = new MediatorLiveData<>();

    private LiveData<PharmacyEntity> pharmacySource;
    private LiveData<HospitalEntity> hospitalSource;
    private LiveData<List<MedicineEntity>> medicineSource;

    @Inject
    public OwnerViewModel(PharmacyRepository pharmacyRepository,
                          HospitalRepository hospitalRepository,
                          MedicineRepository medicineRepository) {
        this.pharmacyRepository = pharmacyRepository;
        this.hospitalRepository = hospitalRepository;
        this.medicineRepository = medicineRepository;
    }

    // Pharmacy -----------------------------------------------------------

    public void loadPharmacy(String ownerUid) {
        LiveData<PharmacyEntity> source = pharmacyRepository.getByOwner(ownerUid);
        if (pharmacySource != null) {
            pharmacy.removeSource(pharmacySource);
        }
        pharmacySource = source;
        pharmacy.addSource(source, pharmacy::setValue);
    }

    public LiveData<PharmacyEntity> getPharmacy() {
        return pharmacy;
    }

    public void createPharmacy(PharmacyEntity entity, Runnable onSaved) {
        pharmacyRepository.createPharmacyAndNotify(entity, onSaved);
    }

    public void loadMedicines(int pharmacyId) {
        LiveData<List<MedicineEntity>> source = medicineRepository.getByPharmacy(pharmacyId);
        if (medicineSource != null) {
            medicines.removeSource(medicineSource);
        }
        medicineSource = source;
        medicines.addSource(source, medicines::setValue);
    }

    public LiveData<List<MedicineEntity>> getMedicines() {
        return medicines;
    }

    public void addMedicine(MedicineEntity entity, Runnable onSaved) {
        medicineRepository.addMedicineAndNotify(entity, onSaved);
    }

    public void updateMedicine(MedicineEntity entity) {
        medicineRepository.updateMedicine(entity);
    }

    public void deleteMedicine(MedicineEntity entity) {
        medicineRepository.deleteMedicine(entity);
    }

    // Hospital -----------------------------------------------------------

    public void loadHospital(String ownerUid) {
        LiveData<HospitalEntity> source = hospitalRepository.getByOwner(ownerUid);
        if (hospitalSource != null) {
            hospital.removeSource(hospitalSource);
        }
        hospitalSource = source;
        hospital.addSource(source, hospital::setValue);
    }

    public LiveData<HospitalEntity> getHospital() {
        return hospital;
    }

    public void createHospital(HospitalEntity entity, Runnable onSaved) {
        hospitalRepository.createHospitalAndNotify(entity, onSaved);
    }

    public void updateHospital(HospitalEntity entity, Runnable onSaved) {
        hospitalRepository.updateHospitalAndNotify(entity, onSaved);
    }
}
