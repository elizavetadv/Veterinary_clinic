package by.overone.clinic.dao.impl;

import by.overone.clinic.controller.exception.ExceptionCode;
import by.overone.clinic.dao.DoctorTimetableDAO;
import by.overone.clinic.dao.exception.DAOIncorrectDataException;
import by.overone.clinic.dao.exception.DAONotExistException;
import by.overone.clinic.dto.DocTimetableDTO;
import by.overone.clinic.dto.DoctorTimetableDTO;
import by.overone.clinic.model.RecordStatus;
import by.overone.clinic.util.constant.DoctorTimetableConstant;
import by.overone.clinic.util.constant.UserConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DoctorTimetableDAOImpl implements DoctorTimetableDAO {

    public final JdbcTemplate jdbcTemplate;

    public static final String ADD_TO_DOCTOR_TIMETABLE_QUERY = "INSERT INTO " + DoctorTimetableConstant.TABLE_TIMETABLE +
            " VALUES(0, ?, ?, ?, ?, ?)";

    private final static String GET_ALL_RECORDS_BY_DOCTOR_ID_QUERY = "SELECT * FROM " + DoctorTimetableConstant.TABLE_TIMETABLE +
            " WHERE " + DoctorTimetableConstant.DOCTOR_ID + "=?";

    private final static String GET_RECORD_BY_ID_QUERY = "SELECT * FROM " + DoctorTimetableConstant.TABLE_TIMETABLE + " WHERE " +
            DoctorTimetableConstant.ID + "=? AND " + UserConstant.STATUS + "<>'CANCELLED'";

    private final static String GET_RECORD_BY_DAY_QUERY = "SELECT * FROM " + DoctorTimetableConstant.TABLE_TIMETABLE +
            " WHERE EXTRACT(DAY FROM date) =? AND " + DoctorTimetableConstant.DOCTOR_ID + "=?";

    private final static String GET_RECORD_BY_MONTH_QUERY = "SELECT * FROM " + DoctorTimetableConstant.TABLE_TIMETABLE +
            " WHERE EXTRACT(MONTH FROM date) =? AND " + DoctorTimetableConstant.DOCTOR_ID + "=?";

    private final static String GET_RECORD_BY_YEAR_QUERY = "SELECT * FROM " + DoctorTimetableConstant.TABLE_TIMETABLE +
            " WHERE EXTRACT(YEAR FROM date) =? AND " + DoctorTimetableConstant.DOCTOR_ID + "=?";

    @Override
    public void addToDoctorTimetable(DoctorTimetableDTO doctorTimetableDTO) {
        jdbcTemplate.update(ADD_TO_DOCTOR_TIMETABLE_QUERY, doctorTimetableDTO.getClientSurname(), doctorTimetableDTO.getDate(),
                doctorTimetableDTO.getTime(), doctorTimetableDTO.getDoctorId(), RecordStatus.CONFIRMED.toString());
    }

    @Override
    public DocTimetableDTO getRecordById(long id) {
        List<DocTimetableDTO> records = jdbcTemplate.query(GET_RECORD_BY_ID_QUERY, new Object[]{id},
                new BeanPropertyRowMapper<>(DocTimetableDTO.class));
        if (records.isEmpty()) {
            throw new DAONotExistException(ExceptionCode.NOT_EXISTING_RECORD.getErrorCode());
        }
        return records.get(0);
    }

    @Override
    public List<DocTimetableDTO> getRecordByDate(int id, int day, int month, int year) {
        StringBuffer sql = new StringBuffer("SELECT * FROM " + DoctorTimetableConstant.TABLE_TIMETABLE + " WHERE ");
        Object[] date = new Integer[4];

        if (day > 0 || day < 32) {
            sql.append("EXTRACT(DAY FROM date) =?");
            date[0] = day;
        }
        if (month > 0 || month < 13) {
            sql.append(" AND EXTRACT(MONTH FROM date) =?");
            if (date[0] == null) {
                date[0] = month;
            } else {
                date[1] = month;
            }
        }
        if (year >= 2022) {
            if(date[0] == null){
                date[0] = year;
            } else if(date[1] == null){
                date[1] = year;
            } else{
                date[2] = year;
            }
            sql.append(" AND EXTRACT(YEAR FROM date) =?");
        }

        sql.append(" AND " + DoctorTimetableConstant.DOCTOR_ID + "=?");
        date[3] = id;

        log.info(date[0] + " " + date[1] + " " + date[2] + " " + date[3]);

        return jdbcTemplate.query(sql.toString(), date, new BeanPropertyRowMapper<>(DocTimetableDTO.class));
    }

    @Override
    public List<DocTimetableDTO> getRecordsByDay(long id, int day) {
        if (day <= 0 || day >= 32) {
            throw new DAOIncorrectDataException(ExceptionCode.INCORRECT_DAY.getErrorCode());
        } else {
            return jdbcTemplate.query(GET_RECORD_BY_DAY_QUERY, new Object[]{day, id}, new BeanPropertyRowMapper<>(DocTimetableDTO.class));
        }
    }

    @Override
    public List<DocTimetableDTO> getRecordsByMonth(long id, int month) {
        if (month <= 0 || month >= 13) {
            throw new DAOIncorrectDataException(ExceptionCode.INCORRECT_MONTH.getErrorCode());
        } else {
            return jdbcTemplate.query(GET_RECORD_BY_MONTH_QUERY, new Object[]{month, id}, new BeanPropertyRowMapper<>(DocTimetableDTO.class));
        }
    }

    @Override
    public List<DocTimetableDTO> getRecordsByYear(long id, int year) {
        if (year >= 2022) {
            return jdbcTemplate.query(GET_RECORD_BY_YEAR_QUERY, new Object[]{year, id}, new BeanPropertyRowMapper<>(DocTimetableDTO.class));
        } else {
            throw new DAOIncorrectDataException(ExceptionCode.INCORRECT_YEAR.getErrorCode());
        }
    }

    @Override
    public List<DocTimetableDTO> getAllByDoctorId(long id) {
        return jdbcTemplate.query(GET_ALL_RECORDS_BY_DOCTOR_ID_QUERY, new Object[]{id},
                new BeanPropertyRowMapper<>(DocTimetableDTO.class));
    }
}
