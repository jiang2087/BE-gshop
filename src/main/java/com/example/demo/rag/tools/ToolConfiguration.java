package com.example.demo.rag.tools;

import com.example.demo.rag.tools.implementations.GetProductByIdTool;
import com.example.demo.rag.tools.implementations.GetProductsTool;
import com.example.demo.rag.tools.implementations.GetTopPurchasersTool;
import com.example.demo.rag.tools.implementations.GetTopProductsByPurchaseCountTool;
import com.example.demo.rag.tools.implementations.GetMostPurchasedProductsTool;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ToolConfiguration {

    private final ToolRegistry toolRegistry;
    private final GetProductByIdTool getProductByIdTool;
    private final GetProductsTool getProductsTool;
    private final GetTopPurchasersTool getTopPurchasersTool;
    private final GetTopProductsByPurchaseCountTool getTopProductsByPurchaseCountTool;
    private final GetMostPurchasedProductsTool getMostPurchasedProductsTool;

    @PostConstruct
    public void registerTools() {
        toolRegistry.registerTool(getProductByIdTool, getProductByIdTool);
        toolRegistry.registerTool(getProductsTool, getProductsTool);
        toolRegistry.registerTool(getProductsTool, getProductsTool);
        toolRegistry.registerTool(getTopPurchasersTool, getTopPurchasersTool);
        toolRegistry.registerTool(getTopProductsByPurchaseCountTool, getTopProductsByPurchaseCountTool);
        toolRegistry.registerTool(getMostPurchasedProductsTool, getMostPurchasedProductsTool);
    }
}
