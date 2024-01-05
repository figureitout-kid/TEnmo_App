package com.techelevator.tenmo.dao;
import com.techelevator.tenmo.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class JdbcAccountDao implements AccountDao {
    private final JdbcTemplate jdbcTemplate;
/////////////////////////////////////////////curious about using DataSource over JdbcTemplate jdbcTemplate??
    @Autowired
    public JdbcAccountDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public BigDecimal getBalance(int accountId) {
        String sql = "SELECT balance " +
                "FROM account " +
                "WHERE account_id = ?;";

        SqlRowSet row = jdbcTemplate.queryForRowSet(sql, accountId);

        BigDecimal balance = null;
        if(row.next())
        {
            balance = row.getBigDecimal("balance");
        }
        return balance;
    }

    @Override
    public void updateBalance(int accountId, BigDecimal amount) {
        String sql = "UPDATE account SET balance = ? WHERE account_id = ?";
        jdbcTemplate.update(sql, amount, accountId);
    }
    @Override
    public Account getAccountByUserId(int userId){
        String sql = "SELECT account_id, user_id, balance FROM account WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, new AccountRowMapper(), userId);
    }
    @Override
    public List<Account> listAllAccounts(){
        String sql = "SELECT account_id, user_id, balance FROM account";
        return jdbcTemplate.query(sql, new AccountRowMapper());
    }
    @Override
    public Account getAccountByAccountId(int accountId){
        String sql = "SELECT account_id, user_id, balance FROM account WHERE account_id = ?";
        return jdbcTemplate.queryForObject(sql, new AccountRowMapper(), accountId);
    }
    @Override
    public Integer getAccountIdByUserId(int userId){
        String sql = "SELECT a.account_id " +
                "FROM account a " +
                "JOIN tenmo_user u ON u.user_id = a.user_id " +
                "WHERE u.user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final class AccountRowMapper implements RowMapper<Account>{
        @Override
        public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
            Account account = new Account();
            account.setAccountId(rs.getInt("account_id"));
            account.setUserId(rs.getInt("user_id"));
            account.setBalance(rs.getBigDecimal("balance"));
            return account;
        }
    }

}