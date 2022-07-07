#!/usr/bin/env bash

mkdir microservices
cd microservices

spring init \
--boot-version=2.6.7.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=meal-service \
--package-name=se.magnus.microservices.core.meal \
--groupId=se.magnus.microservices.core.meal \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
meal-service

spring init \
--boot-version=2.6.7.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=ingredient-service \
--package-name=se.magnus.microservices.core.ingredient \
--groupId=se.magnus.microservices.core.ingredient \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
ingredient-service

spring init \
--boot-version=2.6.7.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=comment-service \
--package-name=se.magnus.microservices.core.comment \
--groupId=se.magnus.microservices.core.comment \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
comment-service

spring init \
--boot-version=2.6.7.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=recommended-drink-service \
--package-name=se.magnus.microservices.core.recommended-drink \
--groupId=se.magnus.microservices.core.recommended-drink \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
recommended-drink-service

spring init \
--boot-version=2.6.7.RELEASE \
--build=gradle \
--java-version=1.8 \
--packaging=jar \
--name=meal-composite-service \
--package-name=se.magnus.microservices.composite.meal \
--groupId=se.magnus.microservices.composite.meal \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
meal-composite-service

cd ..