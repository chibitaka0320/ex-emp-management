package com.example.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.Employee;
import com.example.repository.EmployeeRepository;

/**
 * 管理者関連機能の業務処理を行うサービス
 *
 * @author T.Araki
 */
@Service
@Transactional
public class EmployeeService {

    /** employeeRepository */
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * 従業員情報を全件取得
     *
     * @return 従業員情報リスト
     */
    public Map<Integer, List<Employee>> showList() {
        int limit = 10;
        int offset = 0;
        Map<Integer, List<Employee>> employeeListMap = new LinkedHashMap<>();
        int count = employeeRepository.countAll();
        int page = count / limit + 1;
        if (count % limit == 0) {
            count--;
        }

        for (int i = 1; i <= page; i++) {
            employeeListMap.put(i, employeeRepository.findAllLimitOffset(limit, offset));
            offset += limit;
        }

        return employeeListMap;
    }

    /**
     * 従業員情報の取得
     *
     * @param id 従業員ID
     * @return 従業員情報
     */
    public Employee showDetail(Integer id) {
        return employeeRepository.load(id);
    }

    /**
     * 従業員情報の更新
     *
     * @param employee 従業員情報
     */
    public void update(Employee employee) {
        employeeRepository.update(employee);
    }

    /**
     * 従業員情報を入社日の名前と期間で検索
     *
     * @param name    名前
     * @param started 入社日(開始期間)
     * @param ended   入社日(終了期間)
     * @return 従業員情報リスト
     */
    public List<Employee> serach(String name, String started, String ended) {
        if (name.isEmpty() && started.isEmpty() && ended.isEmpty()) {
            return employeeRepository.findAll();
        } else if (started.isEmpty() && ended.isEmpty()) {
            return employeeRepository.findByName(name);
        } else if (name.isEmpty()) {
            return employeeRepository.findByHireDate(started, ended);
        } else {
            return employeeRepository.findByNameHireDate(name, started, ended);
        }
    }
}
