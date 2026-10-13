package com.dcu.practicum.loader;

import com.dcu.practicum.model.FinancialTransaction;
import java.io.Reader;
import java.util.List;

public interface DatasetParser {
    List<FinancialTransaction> parse(Reader reader) throws Exception;
}