package com.example.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.domain.Employee;
import com.example.form.LoginForm;
import com.example.form.UpdateEmployeeForm;
import com.example.service.EmployeeService;

import jakarta.servlet.http.HttpSession;

/**
 * 従業員関連画面を表示する処理を記述する
 *
 * @author T.Araki
 */
@Controller
@RequestMapping("/employee")
public class EmployeeController {

    /** セッションスコープ */
    @Autowired
    private HttpSession session;

    /** employeeService */
    @Autowired
    private EmployeeService employeeService;

    /**
     * EmployeeFormオブジェクトの作成
     *
     * @return EmployeeFormオブジェクト
     */
    @ModelAttribute
    private UpdateEmployeeForm setUpUpdateEmployeeForm() {
        return new UpdateEmployeeForm();
    }

    /**
     * LoginFormオブジェクトの作成
     *
     * @return LoginFormオブジェクト
     */
    @ModelAttribute
    private LoginForm setUpUpdateLoginForm() {
        return new LoginForm();
    }

    /**
     * 従業員一覧を出力する
     *
     * @param page  ページ番号(デフォルトは１ページ)
     * @param model モデル
     * @return 従業員一覧画面
     */
    @GetMapping("/showList")
    public String showList(@RequestParam(defaultValue = "1") int page, Model model) {
        if (session.getAttribute("administratorName") == null) {
            return "forward:/";
        }
        Map<Integer, List<Employee>> employeeListMap = employeeService.showList();
        model.addAttribute("page", employeeListMap.size());
        model.addAttribute("employeeList", employeeListMap.get(page));
        return "employee/list";
    }

    /**
     * 従業員詳細を出力する
     *
     * @param model モデル
     * @param form  従業員更新フォーム
     * @return 従業員詳細画面
     */
    @GetMapping("/showDetail")
    public String showDetail(String id, Model model, UpdateEmployeeForm form) {
        if (session.getAttribute("administratorName") == null) {
            return "forward:/";
        }
        model.addAttribute("employee", employeeService.showDetail(Integer.parseInt(id)));
        return "employee/detail";
    }

    /**
     * 従業員詳細を更新する
     *
     * @param form 従業員更新フォーム
     * @return 従業員一覧画面(リダイレクト)
     */
    @PostMapping("/update")
    public String update(@Validated UpdateEmployeeForm form, BindingResult result,
            Model model) {
        if (session.getAttribute("administratorName") == null) {
            return "forward:/";
        }
        if (result.hasErrors()) {
            return showDetail(form.getId(), model, form);
        }

        MultipartFile image = form.getImage();
        String imageName = image.getOriginalFilename();
        String imagePath = "src/main/resources/static/img/" + imageName;

        try {
            Path filePath = Paths.get(imagePath);
            // ディレクトリが存在しない場合は作成
            Files.createDirectories(filePath.getParent());
            byte[] content = image.getBytes();
            Files.write(filePath, content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Employee employee = new Employee();
        BeanUtils.copyProperties(form, employee);
        employee.setId(Integer.parseInt(form.getId()));

        if (imageName != null) {
            employee.setImage(imageName);
        }

        employee.setHireDate(Date.valueOf(form.getHireDate()));
        employee.setSalary(Integer.parseInt(form.getSalary()));
        employee.setDependentsCount(Integer.parseInt(form.getDependentsCount()));

        employeeService.update(employee);
        return "redirect:/employee/showList";
    }

    /**
     * 従業員情報を入社日の名前と期間で検索
     *
     * @param name    名前
     * @param started 入社日(開始期間)
     * @param ended   入社日(終了期間)
     * @return 従業員情報リスト
     */
    @PostMapping("/search")
    public String search(String name, String started, String ended, Model model) {
        model.addAttribute("employeeList", employeeService.serach(name, started, ended));
        return "employee/list";
    }

}
