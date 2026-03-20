package com.pm.userservice.filter;

import com.pm.userservice.dto.AdminFilterDto;
import com.pm.userservice.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SpecFilter {

    private SpecFilter() {
    }

    public static Specification<User> byFilter(AdminFilterDto filter) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filter.email() != null && !filter.email().isBlank()) {

                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + filter.email().trim().toLowerCase() + "%"));
            }

            if (filter.birthDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthDate"), filter.birthDateFrom()));
            }
            if (filter.birthDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthDate"), filter.birthDateTo()));
            }

            // registeredDate range
            if (filter.registerDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("registeredDate"), filter.registerDateFrom()));
            }
            if (filter.registerDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("registeredDate"), filter.registerDateTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    }
}
