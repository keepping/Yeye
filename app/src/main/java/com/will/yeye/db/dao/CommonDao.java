package com.will.yeye.db.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;
import com.will.yeye.db.DatabaseHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CommonDao<T> {

    private Dao<T, Integer> mDao;
    private DatabaseHelper mHelper;

    public CommonDao(DatabaseHelper helper,Class<?> clazz) {
        try {
            mHelper = helper;
            mDao = mHelper.getDao(clazz);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param
     */
    public int add(T entity) {
        try {
            return mDao.create(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 删除一条记录
     */
    public void delete(T entity) {
        try {
            mDao.delete(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAll(Class<?> dataClass){
        try {
            TableUtils.clearTable(mDao.getConnectionSource(), dataClass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 更新一条记录
     */
    public void update(T entity) {
        try {
            mDao.update(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询一条记录
     * @param id
     * @return
     */
    public T queryById(int id) {
        T data = null;
        try {
            data = mDao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public T queryForFirst() throws SQLException{
        List<T> datas = queryAll();
        if(datas != null && !datas.isEmpty()){
            return datas.get(0);
        }
        return null;
    }


    /**
     * 查询所有记录
     * @return
     */
    public List<T> queryAll() {
        List<T> datas = new ArrayList<T>();
        try {
           datas = mDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datas;
    }

    /***
     *
     * @param orderByKey
     * @param ascending
     * @param eqs
     * @return
     * @throws SQLException
     */
    public List<T> queryByCondition(String orderByKey,boolean ascending,Map<String,Object> eqs)  throws SQLException{
        QueryBuilder builder = mDao.queryBuilder();
        Where where = builder.orderBy(orderByKey,ascending).where();
        int size = eqs.size();
        int index = 0;
        for(Map.Entry<String,Object> eq:eqs.entrySet()){
            String key = eq.getKey();
            Object value = eq.getValue();
            if(key != null && !"".equals(key) && value != null){
                if(index ++ != size - 1 ){
                    where = where.eq(key,value).and();
                }
                else{
                    where = where.eq(key,value);
                }
            }
            else {
                continue;
            }
        }
        return builder.query();
    }

    public T queryByConditionSingle(String orderByKey,boolean ascending,Map<String,Object> eqs)  throws SQLException{
        QueryBuilder builder = mDao.queryBuilder();
        Where where = builder.orderBy(orderByKey,ascending).where();
        int size = eqs.size();
        int index = 0;
        for(Map.Entry<String,Object> eq:eqs.entrySet()){
            String key = eq.getKey();
            Object value = eq.getValue();
            if(key != null && !"".equals(key) && value != null){
                if(index ++ != size - 1 ){
                    where = where.eq(key,value).and();
                }
                else{
                    where = where.eq(key,value);
                }
            }
            else {
                continue;
            }
        }
        List<T> results = builder.query();
        if(results == null || results.isEmpty()){
            return null;
        }
        return results.get(0);
    }

    public void update(Map<String,Object> eqs,Map<String,Object> updates) throws SQLException{
        UpdateBuilder updateBuilder = mDao.updateBuilder();
        Where where = updateBuilder.where();
        int size = eqs.size();
        int index = 0;
        for(Map.Entry<String,Object> eq:eqs.entrySet()){
            String key = eq.getKey();
            Object value = eq.getValue();
            if(key != null && !"".equals(key) && value != null){
                if(index ++ != size - 1 ){
                    where = where.eq(key,value).and();
                }
                else{
                    where = where.eq(key,value);
                }
            }
            else {
                continue;
            }
        }

        for(Map.Entry<String,Object> update:updates.entrySet()){
            String key = update.getKey();
            Object value = update.getValue();
            if(key != null && !"".equals(key) && value != null){
                updateBuilder = updateBuilder.updateColumnValue(key,value);
            }
            else{
                continue;
            }
        }
        updateBuilder.update();
    }

    public void delete(Map<String,Object> eqs) throws SQLException {
        DeleteBuilder deleteBuilder = mDao.deleteBuilder();
        Where where = deleteBuilder.where();
        int size = eqs.size();
        int index = 0;
        for(Map.Entry<String,Object> eq:eqs.entrySet()){
            String key = eq.getKey();
            Object value = eq.getValue();
            if(key != null && !"".equals(key) && value != null){
                if(index ++ != size - 1 ){
                    where = where.eq(key,value).and();
                }
                else{
                    where = where.eq(key,value);
                }
            }
            else {
                continue;
            }
        }
        deleteBuilder.delete();
    }


}
