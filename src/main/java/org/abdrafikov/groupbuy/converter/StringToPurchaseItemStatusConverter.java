package org.abdrafikov.groupbuy.converter;

import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StringToPurchaseItemStatusConverter implements Converter<String, PurchaseItemStatus> {

    @Override
    public PurchaseItemStatus convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        return PurchaseItemStatus.valueOf(source.trim().toUpperCase(Locale.ROOT));
    }
}
