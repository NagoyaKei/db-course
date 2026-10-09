package pro.gorbunov.db_course.repository;

import lombok.experimental.UtilityClass;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@UtilityClass
class SqlParams {

    SqlParameterSource of(Object bean) {
        return new BeanPropertySqlParameterSource(bean) {
            @Override
            public Object getValue(String paramName) {
                Object value = super.getValue(paramName);
                return value instanceof Enum<?> enumValue ? enumValue.name() : value;
            }
        };
    }
}
