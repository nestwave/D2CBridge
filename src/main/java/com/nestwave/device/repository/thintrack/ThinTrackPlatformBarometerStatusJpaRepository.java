package com.nestwave.device.repository.thintrack;

import com.nestwave.device.repository.CompositeKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ThinTrackPlatformBarometerStatusJpaRepository extends JpaRepository<ThinTrackPlatformBarometerStatusRecord, Long> {
    Optional<ThinTrackPlatformBarometerStatusRecord> findByKey(CompositeKey key);
    List<ThinTrackPlatformBarometerStatusRecord> findAllByKeyIdOrderByKeyUtcTimeAsc(long id);
    void deleteByKeyId(long id);
}
