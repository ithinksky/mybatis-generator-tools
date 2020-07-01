package ${basePackage}.controller.manager;
import com.ithinksky.http.ApiResult;
import ${basePackage}.model.entity.${modelNameUpperCamel}Entity;
import ${basePackage}.service.I${modelNameUpperCamel}Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import com.ithinksky.base.controller.BaseController;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.util.List;

/**
* Created by ${author} on ${date}.
*/
@Slf4j
@RestController
@RequestMapping(value = "${baseRequestMapping}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Api(value = "Manager WEB - Manager${modelNameUpperCamel}Controller", description = " XXX ", produces = MediaType
.APPLICATION_JSON_UTF8_VALUE)
public class Manager${modelNameUpperCamel}Controller extends BaseController {
    @Autowired
    private I${modelNameUpperCamel}Service i${modelNameUpperCamel}Service;

    @PostMapping("/add${modelNameUpperCamel}")
    public ApiResult add${modelNameUpperCamel}(@RequestBody @Valid ${modelNameUpperCamel}Entity ${modelNameLowerCamel}Entity) {
        i${modelNameUpperCamel}Service.save(${modelNameLowerCamel}Entity);
        return ApiResult.SUCCESS;
    }

    @PostMapping("/delete${modelNameUpperCamel}")
    public ApiResult delete${modelNameUpperCamel}(@RequestParam Integer id) {
        i${modelNameUpperCamel}Service.deleteByKey(id);
        return ApiResult.SUCCESS;
    }

    @PostMapping("/update${modelNameUpperCamel}")
    public ApiResult update${modelNameUpperCamel}(@RequestBody @Valid ${modelNameUpperCamel}Entity ${modelNameLowerCamel}Entity) {
        i${modelNameUpperCamel}Service.update(${modelNameLowerCamel}Entity);
        return ApiResult.SUCCESS;
    }

    @PostMapping("/find${modelNameUpperCamel}Detail")
    public ApiResult find${modelNameUpperCamel}Detail(@RequestParam Integer id) {
        ${modelNameUpperCamel}Entity ${modelNameLowerCamel}Entity = i${modelNameUpperCamel}Service.selectByKey(id);
        return ApiResult.success(${modelNameLowerCamel}Entity);
    }

    @PostMapping("/find${modelNameUpperCamel}List")
    public ApiResult find${modelNameUpperCamel}List(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer size,
                           @RequestBody ${modelNameUpperCamel}Entity ${modelNameLowerCamel}Entity) {
        List<${modelNameUpperCamel}Entity> list = i${modelNameUpperCamel}Service.select(${modelNameLowerCamel}Entity);
        return ApiResult.success(list);
    }
}
