package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.ProcedureRunner;
import com.superstefo.automatic.investor.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class StartingProcedure implements Startable {
    private final InvestProps investProps;
    private final WalletService wallet;
    private ProcedureRunner procedureRunner;

    private final List<String> excludedFields = List.of("password");

    @Override
    public void start() {
        CompletableFuture<BigDecimal> future = wallet.updateFreeInvestorsMoneyFromServer();
        logOnceAtStart();
        procedureRunner.nextRunFindLoansProcedure();
        future.thenAccept(money -> log.info("Free investor's funds: {}", money));
    }

    private void logField(String fieldName) {
        String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            Method getterMethod = investProps.getClass().getMethod(getterName);
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

    @Autowired
    public void setProcedureRunner(@Lazy ProcedureRunner procedureRunner) {
        this.procedureRunner = procedureRunner;
    }
}