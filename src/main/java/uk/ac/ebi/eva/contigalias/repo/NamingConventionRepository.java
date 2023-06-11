package uk.ac.ebi.eva.contigalias.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.contigalias.entities.NamingConvention;
@Repository
public interface NamingConventionRepository extends JpaRepository<NamingConvention, Long> {
}
