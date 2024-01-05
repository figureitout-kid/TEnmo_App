package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {
    private final JdbcTemplate jdbcTemplate;

    private JdbcAccountDao jdbcAccountDao;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate =  jdbcTemplate;
    }


    @Override
    public Transfer createTransfer(Transfer transfer) {
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES (?,?,?,?,?) RETURNING transfer_id";

        jdbcTemplate.update(sql, transfer);
        return null;
    }


    @Override
    public List<Transfer> getTransfersByAccountId(int accountId){
        String sql = "SELECT * FROM transfer WHERE account_from = ? OR account_to = ?";
        return jdbcTemplate.query(sql, new TransferMapper(), accountId, accountId);
    }

    @Override
    public Transfer findTransferById(int transferId) {
        String sql = "SELECT * FROM transfer WHERE transfer_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new TransferMapper(), transferId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
    @Override
    public List<Transfer> getPendingTransfers(int accountId) {
        String sql = "SELECT * FROM transfer WHERE (account_from = ? OR account_to = ?) AND transfer_status_id = 1";
        return jdbcTemplate.query(sql, new TransferMapper(), accountId, accountId);
    }

    @Override
    public List<Transfer> getTransfersByUsername(String username){
        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, " +
                "t.account_from, t.account_to, t.amount " +
                "FROM transfer t " +
                "JOIN account a ON a.account_id = t.account_from OR a.account_id = t.account_to " +
                "JOIN tenmo_user u ON u.user_id = a.user_id " +
                "WHERE u.username = ?";
        return jdbcTemplate.query(sql, new TransferMapper(), username);
    }
    @Override
    public boolean updateTransferStatus(int transferId, int transferStatusId) {
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?";
        return jdbcTemplate.update(sql, transferStatusId, transferId) ==  1;
    }

    private static class TransferMapper implements RowMapper<Transfer> {
        @Override
        public Transfer mapRow (ResultSet rs, int rowNum) throws SQLException {
            Transfer transfer = new Transfer();
            transfer.setTransferId(rs.getInt("transfer_id"));
            transfer.setTransferTypeId(rs.getInt("transfer_type_id"));
            transfer.setTransferStatusId(rs.getInt("transfer_status_id"));
            transfer.setAccountFrom(rs.getInt("account_from"));
            transfer.setAccountTo(rs.getInt("account_to"));
            transfer.setAmount(rs.getBigDecimal("amount"));
            return transfer;
        }
    }

    // this is a table join, this is potentially needed, not in this format as TransferDetail class doesn't exist--
    //but we need to investigate if we do need to join tables in order to communicate and alter database with account/balance based off accountId and/or userId???
//    public List<TransferDetail> getTransferDetailsByUserId(int userId) {
//        String sql = "SELECT t.transfer_id, t.amount, t.transfer_status_id, t.transfer_type_id, "
//                + "a_from.account_id as from_account, a_to.account_id as to_account, "
//                + "u_from.username as from_username, u_to.username as to_username "
//                + "FROM transfer t "
//                + "JOIN account a_from ON t.account_from = a_from.account_id "
//                + "JOIN account a_to ON t.account_to = a_to.account_id "
//                + "JOIN tenmo_user u_from ON a_from.user_id = u_from.user_id "
//                + "JOIN tenmo_user u_to ON a_to.user_id = u_to.user_id "
//                + "WHERE a_from.user_id = ? OR a_to.user_id = ?";
//        return jdbcTemplate.query(sql, new TransferDetailMapper(), userId, userId);
//    }

}
