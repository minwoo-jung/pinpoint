/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.batch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import org.springframework.batch.item.database.AbstractCursorItemReader;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author minwoo.jung
 * 
 * ref http://devcafe.nhncorp.com/batch/forum/511830
 *
 * @param <T>
 */
public class CubridCursorItemReader<T> extends AbstractCursorItemReader<T>{
	

	PreparedStatement preparedStatement;

	PreparedStatementSetter preparedStatementSetter;

	String sql;

	RowMapper<T> rowMapper;

	public CubridCursorItemReader() {
		super();
		setName(ClassUtils.getShortName(CubridCursorItemReader.class));
	}

	/**
	 * Set the RowMapper to be used for all calls to read().
	 * 
	 * @param rowMapper
	 */
	public void setRowMapper(RowMapper<T> rowMapper) {
		this.rowMapper = rowMapper;
	}

	/**
	 * Set the SQL statement to be used when creating the cursor. This statement
	 * should be a complete and valid SQL statement, as it will be run directly
	 * without any modification.
	 * 
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * Set the PreparedStatementSetter to use if any parameter values that need
	 * to be set in the supplied query.
	 * 
	 * @param preparedStatementSetter
	 */
	public void setPreparedStatementSetter(PreparedStatementSetter preparedStatementSetter) {
		this.preparedStatementSetter = preparedStatementSetter;
	}

	/**
	 * Assert that mandatory properties are set.
	 * 
	 * @throws IllegalArgumentException if either data source or sql properties
	 * not set.
	 */
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Assert.notNull(sql, "The SQL query must be provided");
		Assert.notNull(rowMapper, "RowMapper must be provided");
	}


	protected void openCursor(Connection con) {	
		try {
			if (isUseSharedExtendedConnection()) {
				preparedStatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY,
						ResultSet.HOLD_CURSORS_OVER_COMMIT);
			}
			else {
				preparedStatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			}
			applyStatementSettings(preparedStatement);
			if (this.preparedStatementSetter != null) {
				preparedStatementSetter.setValues(preparedStatement);
			}
			this.rs = preparedStatement.executeQuery();
			handleWarnings(preparedStatement);
		}
		catch (SQLException se) {
			close();
			throw getExceptionTranslator().translate("Executing query", getSql(), se);
		}
	
	}


	protected T readCursor(ResultSet rs, int currentRow) throws SQLException {
		return rowMapper.mapRow(rs, currentRow);
	}
	
	/**
	 * Close the cursor and database connection.
	 */
	protected void cleanupOnClose() throws Exception {
		JdbcUtils.closeStatement(this.preparedStatement);
	}

	@Override
	public String getSql() {
		return this.sql;
	}

}
