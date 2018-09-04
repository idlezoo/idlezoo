package idlezoo.game.services

import idlezoo.game.domain.TopEntry
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet

@Service
@Transactional
class TopService(private val resourcesService: ResourcesService, private val template: JdbcTemplate) {

    fun building(building: String): List<TopEntry<Int>> =
            template.query("select u.username, a.count as topvalue"
                    + " from animal a"
                    + " join users u"
                    + " on a.user_id=u.id"
                    + " where a.animal_type=?"
                    + " order by a.count desc"
                    + " limit 10",
                    RowMapper { it, _ -> mapTopEntry<Int>(it) },
                    resourcesService.animalIndex(building)
            )


    fun income(): List<TopEntry<Double>> =
            template.query("select username, base_income + perk_income as topvalue"
                    + " from users"
                    + " order by topvalue desc"
                    + " limit 10")
            { it, _ -> mapTopEntry<Double>(it) }

    fun wins(): List<TopEntry<Int>> =
            template.query("select username, fights_win as topvalue"
                    + " from users"
                    + " order by fights_win desc"
                    + " limit 10")
            { it, _ -> mapTopEntry<Int>(it) }


    fun losses(): List<TopEntry<Int>> =
            template.query("select username, fights_loss as topvalue"
                    + " from users"
                    + " order by fights_loss desc"
                    + " limit 10")
            { it, _ -> mapTopEntry<Int>(it) }


    fun championTime(): List<TopEntry<Long>> =
            template.query(
                    "select username,"
                            + " champion_time + coalesce(EXTRACT(EPOCH FROM now() - waiting_for_fight_start)::bigint, 0) as topvalue"
                            + " from users"
                            + " order by topvalue desc"
                            + " limit 10")
            { it, _ -> mapTopEntry<Long>(it) }

    private inline fun <reified T : Any> mapTopEntry(res: ResultSet): TopEntry<T> =
            TopEntry(
                    res.getString("username"),
                    res.getObject("topvalue", T::class.java)
            )
}
