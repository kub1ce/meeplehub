package ru.meeplehub.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.meeplehub.domain.entity.Report;

@RepositoryRestResource(exported = false)
public interface ReportRepository extends CrudRepository<Report, Long> {
}