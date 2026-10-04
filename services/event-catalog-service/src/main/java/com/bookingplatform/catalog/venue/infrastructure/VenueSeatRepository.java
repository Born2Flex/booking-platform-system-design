package com.bookingplatform.catalog.venue.infrastructure;

import com.bookingplatform.catalog.venue.domain.VenueSeat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueSeatRepository extends JpaRepository<VenueSeat, Long> {

    List<VenueSeat> findBySectionIdOrderByRowLabelAscSeatNumberAsc(Long sectionId);
}
