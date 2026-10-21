package com.example.productapi;

import com.example.productapi.product.ProductRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
    }

    @Test
    void createProductReturnsCreatedProductAndLocation() throws Exception {
        String payload = """
                {
                  "sku": "TEST-1001",
                  "name": "Test Product",
                  "description": "A product created by an integration test",
                  "category": "Testing",
                  "price": 19.99,
                  "stock": 10
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/products/")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.sku").value("TEST-1001"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));
    }

    @Test
    void createProductWithDuplicateSkuReturnsConflict() throws Exception {
        createProduct("DUP-1001", "First", "Testing", "10.00", 1);

        String payload = """
                {
                  "sku": "dup-1001",
                  "name": "Second",
                  "category": "Testing",
                  "price": 20.00,
                  "stock": 2
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate resource"))
                .andExpect(jsonPath("$.detail", containsString("dup-1001")));
    }

    @Test
    void createProductWithInvalidPayloadReturnsBadRequest() throws Exception {
        String payload = """
                {
                  "sku": "bad sku!",
                  "name": "",
                  "category": "",
                  "price": -5,
                  "stock": -1
                }
                """;

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.category").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stock").exists());
    }

    @Test
    void getProductByIdReturnsProduct() throws Exception {
        long id = createProduct("GET-1001", "Readable Product", "Testing", "12.34", 7);

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.sku").value("GET-1001"))
                .andExpect(jsonPath("$.name").value("Readable Product"));
    }

    @Test
    void getProductByIdReturnsNotFoundProblem() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail", containsString("999999")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void getProductBySkuIsCaseInsensitive() throws Exception {
        createProduct("CASE-1001", "Case Test", "Testing", "5.00", 1);

        mockMvc.perform(get("/api/v1/products/sku/{sku}", "case-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("CASE-1001"));
    }

    @Test
    void listProductsSupportsFilteringSortingAndPagination() throws Exception {
        createProduct("LIST-1001", "Cheap Phone", "Electronics", "100.00", 5);
        createProduct("LIST-1002", "Expensive Phone", "Electronics", "900.00", 5);
        createProduct("LIST-1003", "Notebook", "Office", "5.00", 5);

        mockMvc.perform(get("/api/v1/products")
                        .param("category", "electronics")
                        .param("minPrice", "50")
                        .param("sortBy", "price")
                        .param("direction", "asc")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].sku").value("LIST-1001"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void listProductsSupportsSearchQuery() throws Exception {
        createProduct("SEARCH-1001", "Blue T-Shirt", "Clothing", "25.00", 5);
        createProduct("SEARCH-1002", "Red Hoodie", "Clothing", "55.00", 5);

        mockMvc.perform(get("/api/v1/products").param("q", "shirt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].sku").value("SEARCH-1001"));
    }

    @Test
    void listProductsRejectsInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/v1/products").param("sortBy", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void updateProductReplacesAllFields() throws Exception {
        long id = createProduct("UPD-1001", "Before", "Testing", "10.00", 1);

        String payload = """
                {
                  "sku": "UPD-2002",
                  "name": "After",
                  "description": "Updated description",
                  "category": "Updated",
                  "price": 42.50,
                  "stock": 25,
                  "active": false
                }
                """;

        mockMvc.perform(put("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.sku").value("UPD-2002"))
                .andExpect(jsonPath("$.name").value("After"))
                .andExpect(jsonPath("$.category").value("Updated"))
                .andExpect(jsonPath("$.price").value(42.50))
                .andExpect(jsonPath("$.stock").value(25))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void patchProductUpdatesOnlyProvidedFields() throws Exception {
        long id = createProduct("PATCH-1001", "Original", "Testing", "10.00", 1);

        String payload = """
                {
                  "price": 9.99,
                  "stock": 50
                }
                """;

        mockMvc.perform(patch("/api/v1/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("PATCH-1001"))
                .andExpect(jsonPath("$.name").value("Original"))
                .andExpect(jsonPath("$.price").value(9.99))
                .andExpect(jsonPath("$.stock").value(50));
    }

    @Test
    void patchProductToExistingSkuReturnsConflict() throws Exception {
        createProduct("PATCH-A", "First", "Testing", "10.00", 1);
        long secondId = createProduct("PATCH-B", "Second", "Testing", "10.00", 1);

        mockMvc.perform(patch("/api/v1/products/{id}", secondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"PATCH-A\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate resource"));
    }

    @Test
    void deleteProductRemovesIt() throws Exception {
        long id = createProduct("DEL-1001", "Disposable", "Testing", "1.00", 1);

        mockMvc.perform(delete("/api/v1/products/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownEndpointReturnsNotFoundProblem() throws Exception {
        mockMvc.perform(get("/api/v1/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail", containsString("does-not-exist")));
    }

    @Test
    void unsupportedMethodReturnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/v1/products"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Method not allowed"));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request body"));
    }

    private long createProduct(String sku, String name, String category, String price, int stock) throws Exception {
        String payload = """
                {
                  "sku": "%s",
                  "name": "%s",
                  "description": "Test description",
                  "category": "%s",
                  "price": %s,
                  "stock": %d,
                  "active": true
                }
                """.formatted(sku, name, category, price, stock);

        MvcResult result = mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }
}
