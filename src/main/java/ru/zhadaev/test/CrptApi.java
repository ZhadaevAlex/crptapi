package ru.zhadaev.test;

import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private static AtomicInteger methodCallCounter;
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.methodCallCounter = new AtomicInteger();
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() ->
        {
            lock.lock();
            try {
                resetMethodCallCounter();
                condition.signal();
            } finally {
                lock.unlock();
            }

            System.out.println("reset");

        }, 0L, 1, this.timeUnit);
    }

    public Goods putIntoCirculation(Goods goods, String signature) throws IOException, InterruptedException {
        methodCallCounter.incrementAndGet();

        lock.lock();
        try {
            while (methodCallCounter.get() > requestLimit) {
                try {
                    System.out.println("wait = " + Thread.currentThread().getName());
                    condition.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Thread interrupted");
                }
            }
        } finally {
            lock.unlock();
        }

        return saveGoods(goods);
    }

    private Goods saveGoods(Goods goods) {
        Goods createdGoods;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Goods> request = new HttpEntity<>(goods, headers);
        try {
            ResponseEntity<Goods> response = restTemplate.postForEntity(URL, request, Goods.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                createdGoods = response.getBody();
            } else {
                throw new UnexpectedStatusException("Unexpected response status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            throw new ClientErrorException("Client error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new ServerErrorException("Server error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            throw new AccessErrorException("Resource access error: " + e.getMessage());
        }

        return createdGoods;
    }

    public class UnexpectedStatusException extends RuntimeException {
        public UnexpectedStatusException(String s) {
            super(s);
        }
    }

    public class ClientErrorException extends RuntimeException {
        public ClientErrorException(String s) {
            super(s);
        }
    }

    public class ServerErrorException extends RuntimeException {
        public ServerErrorException(String s) {
            super(s);
        }
    }
    
    public class AccessErrorException extends RuntimeException {
        public AccessErrorException(String s) {
            super(s);
        }
    }

    public class Description {
        private String participantInn;

        public String getParticipantInn() {
            return participantInn;
        }

        public void setParticipantInn(String participantInn) {
            this.participantInn = participantInn;
        }
    }

    private static void resetMethodCallCounter() {
        methodCallCounter.set(0);
    }

    public class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public String getCertificate_document() {
            return certificate_document;
        }

        public void setCertificate_document(String certificate_document) {
            this.certificate_document = certificate_document;
        }

        public String getCertificate_document_date() {
            return certificate_document_date;
        }

        public void setCertificate_document_date(String certificate_document_date) {
            this.certificate_document_date = certificate_document_date;
        }

        public String getCertificate_document_number() {
            return certificate_document_number;
        }

        public void setCertificate_document_number(String certificate_document_number) {
            this.certificate_document_number = certificate_document_number;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public String getTnved_code() {
            return tnved_code;
        }

        public void setTnved_code(String tnved_code) {
            this.tnved_code = tnved_code;
        }

        public String getUit_code() {
            return uit_code;
        }

        public void setUit_code(String uit_code) {
            this.uit_code = uit_code;
        }

        public String getUitu_code() {
            return uitu_code;
        }

        public void setUitu_code(String uitu_code) {
            this.uitu_code = uitu_code;
        }
    }

    public class Goods {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        List<Product> products = new ArrayList<>();
        private String reg_date;
        private String reg_number;

        public Description getDescription() {
            return description;
        }

        public void setDescription(Description description) {
            this.description = description;
        }

        public String getDoc_id() {
            return doc_id;
        }

        public void setDoc_id(String doc_id) {
            this.doc_id = doc_id;
        }

        public String getDoc_status() {
            return doc_status;
        }

        public void setDoc_status(String doc_status) {
            this.doc_status = doc_status;
        }

        public String getDoc_type() {
            return doc_type;
        }

        public void setDoc_type(String doc_type) {
            this.doc_type = doc_type;
        }

        public boolean isImportRequest() {
            return importRequest;
        }

        public void setImportRequest(boolean importRequest) {
            this.importRequest = importRequest;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public void setOwner_inn(String owner_inn) {
            this.owner_inn = owner_inn;
        }

        public String getParticipant_inn() {
            return participant_inn;
        }

        public void setParticipant_inn(String participant_inn) {
            this.participant_inn = participant_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public void setProducer_inn(String producer_inn) {
            this.producer_inn = producer_inn;
        }

        public String getProduction_date() {
            return production_date;
        }

        public void setProduction_date(String production_date) {
            this.production_date = production_date;
        }

        public String getProduction_type() {
            return production_type;
        }

        public void setProduction_type(String production_type) {
            this.production_type = production_type;
        }

        public List<Product> getProducts() {
            return products;
        }

        public void setProducts(List<Product> products) {
            this.products = products;
        }

        public String getReg_date() {
            return reg_date;
        }

        public void setReg_date(String reg_date) {
            this.reg_date = reg_date;
        }

        public String getReg_number() {
            return reg_number;
        }

        public void setReg_number(String reg_number) {
            this.reg_number = reg_number;
        }
    }
}
