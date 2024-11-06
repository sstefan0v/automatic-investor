package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.NextProcedureSelector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StartingProcedure implements Startable {

    private final InvestProps investProps;
    private final NextProcedureSelector nextProcedureSelector;

    private final List<String> excludedFields = List.of("password");

    @Override
    public void start() {
        logOnceAtStart();
        nextProcedureSelector.findLoansProcedure();
    }

    private void logField(String fieldName) {
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            Method getterMethod = InvestProps.class.getMethod(getterName);
            log.info("{} = {}", fieldName, getterMethod.invoke(investProps));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isToLogField(Field field) {
        return !excludedFields.contains(field.getName());
    }

    private void logOnceAtStart() {
        log.info("================================ Settings: ===================================");
        printFields();
        log.info("==============================================================================");
    }

    private void printFields() {
        Field[] fields = investProps.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (isToLogField(field))
                logField(field.getName());
        }
    }
}