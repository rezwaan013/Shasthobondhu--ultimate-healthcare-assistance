package com.shasthobondhu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.shasthobondhu.app.data.local.entities.MedicineEntity;

import java.util.List;

@Dao
public interface MedicineDao {

    @Insert
    long insert(MedicineEntity medicine);

    @Update
    void update(MedicineEntity medicine);

    @Delete
    void delete(MedicineEntity medicine);

    @Query("SELECT * FROM medicines WHERE "
            + "(name LIKE '%' || :query || '%' OR pharmacyName LIKE '%' || :query || '%') AND "
            + "(:area = '' OR pharmacyArea LIKE '%' || :area || '%') "
            + "ORDER BY name ASC")
    LiveData<List<MedicineEntity>> searchMedicines(String query, String area);

    @Query("SELECT * FROM medicines WHERE pharmacyId = :pharmacyId ORDER BY name ASC")
    LiveData<List<MedicineEntity>> getByPharmacy(int pharmacyId);

    @Query("SELECT COUNT(*) FROM medicines")
    int count();
}
