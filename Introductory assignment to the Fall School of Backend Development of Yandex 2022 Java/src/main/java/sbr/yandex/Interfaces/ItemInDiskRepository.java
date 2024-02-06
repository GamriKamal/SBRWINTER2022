package sbr.yandex.Interfaces;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sbr.yandex.Entities.ItemInDisk;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemInDiskRepository extends JpaRepository<ItemInDisk, Long> {

    boolean existsByType(String type);

    boolean existsById(String id);

    boolean existsByParentId(String parentId);

    boolean existsByDate(Date date);

    List<ItemInDisk> findByParentId(String parentId);

    Optional<ItemInDisk> findById(String parentId);

    @Transactional
    void deleteById(String id);

    Optional<ItemInDisk> findByDate(LocalDateTime date);
}
