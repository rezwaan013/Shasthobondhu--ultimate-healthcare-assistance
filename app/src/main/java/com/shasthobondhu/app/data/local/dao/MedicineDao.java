package com.shasthobondhu.app.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.shasthobondhu.app.data.local.entities.MedicineEntity;
import java.util.List;

@Dao
public interface MedicineDao {
    @Insert
    void insert(MedicineEntity medicine);

    @Query("SELECT * FROM medicines WHERE name LIKE '%' || :query || '%'")
    LiveData<List<MedicineEntity>> searchMedicines(String query);

    @Query("SELECT COUNT(*) FROM medicines")
    int count();
}
