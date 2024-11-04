package com.superstefo.automatic.investor.services.procedures;

import com.superstefo.automatic.investor.config.InvestProps;
import com.superstefo.automatic.investor.services.ProcedureRunner;
import com.superstefo.automatic.investor.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@Setter
@RequiredArgsConstructor
public class StartingProcedure implements Startable {
    private final InvestProps investProps;
    private final WalletService wallet;
    private ProcedureRunner procedureRunner;

    private List<String> excludedFields = List.of("password");

    @Override
    public void start() {
        CompletableFuture<BigDecimal> future = wallet.updateFreeInvestorsMoneyFromServer();
        logOnceAtStart();
        procedureRunner.nextRunFindLoansProcedure();
        future.thenAccept(money -> log.info("Free investor's funds: {}", money));
    }

    private void printFields() {
        Field[] fields = investProps.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (isToLogField(field))
                    log.info("{} = {}", field.getName(), field.get(investProps));
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            } finally {
                field.setAccessible(false);
            }
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

    @Autowired
    public void setProcedureRunner(@Lazy ProcedureRunner procedureRunner) {
        this.procedureRunner = procedureRunner;
    }
}