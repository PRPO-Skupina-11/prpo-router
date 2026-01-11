package com.prpo.router.logic;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

  private final JdbcTemplate jdbc;

  public PricingService(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public PricePer1k getPrice(String providerId, String modelId) {
    return jdbc.query(
        """
        select price_prompt_per_1k, price_completion_per_1k
        from router.models
        where provider_id = ? and id = ?
        """,
        new PriceRowMapper(),
        providerId,
        modelId
    ).stream().findFirst().orElse(null);
  }

  private static final class PriceRowMapper implements RowMapper<PricePer1k> {
    @Override
    public PricePer1k mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new PricePer1k(
          rs.getBigDecimal("price_prompt_per_1k"),
          rs.getBigDecimal("price_completion_per_1k")
      );
    }
  }

  public record PricePer1k(BigDecimal prompt, BigDecimal completion) {}
}
