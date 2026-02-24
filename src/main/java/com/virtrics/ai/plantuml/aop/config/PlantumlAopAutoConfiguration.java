package com.virtrics.ai.plantuml.aop.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({PlantumlAopConfiguration.class})
public class PlantumlAopAutoConfiguration {}
