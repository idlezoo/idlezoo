package idlezoo.game.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import idlezoo.game.domain.TopEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TopService {
    private final JdbcTemplate template;
    private final ResourcesService resourcesService;

    public TopService(ResourcesService resourcesService, JdbcTemplate template) {
        this.resourcesService = resourcesService;
        this.template = template;
    }

    public List<TopEntry<Integer>> building(String building) {
        return template.query("select u.username, a.count as topvalue"
                + " from animal a"
                + " join users u"
                + " on a.user_id=u.id"
                + " where a.animal_type=?"
                + " order by a.count desc"
                + " limit 10", INTEGER_TOP_MAPPER, resourcesService.animalIndex(building));
    }

    public List<TopEntry<Double>> income() {
        return template.query("select username, base_income + perk_income as topvalue"
                + " from users"
                + " order by topvalue desc"
                + " limit 10", DOUBLE_TOP_MAPPER);
    }

    public List<TopEntry<Integer>> wins() {
        return template.query("select username, fights_win as topvalue"
                + " from users"
                + " order by fights_win desc"
                + " limit 10", INTEGER_TOP_MAPPER);
    }


    public List<TopEntry<Integer>> losses() {
        return template.query("select username, fights_loss as topvalue"
                + " from users"
                + " order by fights_loss desc"
                + " limit 10", INTEGER_TOP_MAPPER);
    }


    public List<TopEntry<Long>> championTime() {
        return template.query(
                "select username,"
                        + " champion_time + coalesce(EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint, 0) as topvalue"
                        + " from users"
                        + " order by topvalue desc"
                        + " limit 10",
                LONG_TOP_MAPPER);
    }

    private static final TopEntryRowMapper<Integer> INTEGER_TOP_MAPPER = new TopEntryRowMapper<>(Integer.class);
    private static final TopEntryRowMapper<Long> LONG_TOP_MAPPER = new TopEntryRowMapper<>(Long.class);
    private static final TopEntryRowMapper<Double> DOUBLE_TOP_MAPPER = new TopEntryRowMapper<>(Double.class);

    private static final class TopEntryRowMapper<T> implements RowMapper<TopEntry<T>> {

        private final Class<T> valueClass;

        public TopEntryRowMapper(Class<T> valueClass) {
            this.valueClass = valueClass;
        }

        @Override
        public TopEntry<T> mapRow(ResultSet res, int rowNum) throws SQLException {
            return new TopEntry<T>(res.getString("username"), res.getObject("topvalue", valueClass));
        }
    }
}
