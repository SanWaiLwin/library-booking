package com.swl.booking.system.security;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swl.booking.system.entity.User;

/**
 * Comprehensive test suite for JwtAuthenticationResponse
 * Tests JWT authentication response data transfer object functionality
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationResponseTest {

    private JwtAuthenticationResponse jwtAuthenticationResponse;
    private ObjectMapper objectMapper;
    
    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
    private static final String UPDATED_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.updated";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void constructor_ShouldCreateInstance_WithTokenAndUser() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Act
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Assert
        assertNotNull(jwtAuthenticationResponse);
        assertEquals(TEST_TOKEN, jwtAuthenticationResponse.getToken());
        assertEquals(testUser, jwtAuthenticationResponse.getUser());
    }

    @Test
    void constructor_ShouldCreateInstance_WithNoArgs() {
        // Act
        jwtAuthenticationResponse = new JwtAuthenticationResponse();

        // Assert
        assertNotNull(jwtAuthenticationResponse);
        assertNull(jwtAuthenticationResponse.getToken());
        assertNull(jwtAuthenticationResponse.getUser());
    }

    @Test
    void constructor_ShouldHandleNullToken_Gracefully() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Act
        jwtAuthenticationResponse = new JwtAuthenticationResponse(null, testUser);

        // Assert
        assertNotNull(jwtAuthenticationResponse);
        assertNull(jwtAuthenticationResponse.getToken());
        assertEquals(testUser, jwtAuthenticationResponse.getUser());
    }

    @Test
    void constructor_ShouldHandleNullUser_Gracefully() {
        // Act
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, null);

        // Assert
        assertNotNull(jwtAuthenticationResponse);
        assertEquals(TEST_TOKEN, jwtAuthenticationResponse.getToken());
        assertNull(jwtAuthenticationResponse.getUser());
    }

    @Test
    void constructor_ShouldHandleEmptyToken_Gracefully() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        // Act
        jwtAuthenticationResponse = new JwtAuthenticationResponse("", testUser);

        // Assert
        assertNotNull(jwtAuthenticationResponse);
        assertEquals("", jwtAuthenticationResponse.getToken());
        assertEquals(testUser, jwtAuthenticationResponse.getUser());
    }

    @Test
    void getToken_ShouldReturnCorrectValue() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Act
        String token = jwtAuthenticationResponse.getToken();

        // Assert
        assertEquals(TEST_TOKEN, token);
    }

    @Test
    void setToken_ShouldUpdateValue_Correctly() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Act
        jwtAuthenticationResponse.setToken(UPDATED_TOKEN);

        // Assert
        assertEquals(UPDATED_TOKEN, jwtAuthenticationResponse.getToken());
    }

    @Test
    void setToken_ShouldHandleNull_Gracefully() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Act
        jwtAuthenticationResponse.setToken(null);

        // Assert
        assertNull(jwtAuthenticationResponse.getToken());
    }

    @Test
    void getUser_ShouldReturnCorrectValue() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Act
        User user = jwtAuthenticationResponse.getUser();

        // Assert
        assertEquals(testUser, user);
    }

    @Test
    void setUser_ShouldUpdateValue_Correctly() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);
        
        User updatedUser = new User();
        updatedUser.setId(2L);
        updatedUser.setEmail("updated@example.com");

        // Act
        jwtAuthenticationResponse.setUser(updatedUser);

        // Assert
        assertEquals(updatedUser, jwtAuthenticationResponse.getUser());
    }

    @Test
    void setUser_ShouldHandleNull_Gracefully() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Act
        jwtAuthenticationResponse.setUser(null);

        // Assert
        assertNull(jwtAuthenticationResponse.getUser());
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingDifferentObjects() {
        // Arrange
        User testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setEmail("test@example.com");
        
        User testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setEmail("updated@example.com");
        
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser1);
        JwtAuthenticationResponse otherResponse = new JwtAuthenticationResponse(UPDATED_TOKEN, testUser2);

        // Act
        boolean isEqual = jwtAuthenticationResponse.equals(otherResponse);

        // Assert
        assertFalse(isEqual);
    }

    @Test
    void equals_ShouldReturnTrue_WhenComparingWithSelf() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Act
        boolean isEqual = jwtAuthenticationResponse.equals(jwtAuthenticationResponse);

        // Assert
        assertTrue(isEqual);
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingWithNull() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Act
        boolean isEqual = jwtAuthenticationResponse.equals(null);

        // Assert
        assertFalse(isEqual);
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingWithDifferentClass() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);
        String otherObject = "not a JwtAuthenticationResponse";

        // Act
        boolean isEqual = jwtAuthenticationResponse.equals(otherObject);

        // Assert
        assertFalse(isEqual);
    }

    @Test
    void hashCode_ShouldBeDifferent_ForDifferentObjects() {
        // Arrange
        User testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setEmail("test@example.com");
        
        User testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setEmail("updated@example.com");
        
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser1);
        JwtAuthenticationResponse otherResponse = new JwtAuthenticationResponse(UPDATED_TOKEN, testUser2);

        // Act
        int hashCode1 = jwtAuthenticationResponse.hashCode();
        int hashCode2 = otherResponse.hashCode();

        // Assert
        assertNotEquals(hashCode1, hashCode2);
    }

    @Test
    void toString_ShouldReturnMeaningfulString_WithAllFields() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Act
        String toString = jwtAuthenticationResponse.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("JwtAuthenticationResponse") || 
                  toString.contains(TEST_TOKEN) || 
                  toString.contains("test@example.com"));
    }

    @Test
    void jsonSerialization_ShouldSerializeAndDeserialize_Correctly() throws Exception {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Act - Serialize
        String json = objectMapper.writeValueAsString(jwtAuthenticationResponse);
        
        // Act - Deserialize
        JwtAuthenticationResponse deserializedResponse = 
            objectMapper.readValue(json, JwtAuthenticationResponse.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserializedResponse);
        assertEquals(jwtAuthenticationResponse.getToken(), deserializedResponse.getToken());
        assertEquals(jwtAuthenticationResponse.getUser().getId(), deserializedResponse.getUser().getId());
        assertEquals(jwtAuthenticationResponse.getUser().getEmail(), deserializedResponse.getUser().getEmail());
    }

    @Test
    void jsonSerialization_ShouldHandleNullValues_Correctly() throws Exception {
        // Arrange
        jwtAuthenticationResponse = new JwtAuthenticationResponse(null, null);

        // Act - Serialize
        String json = objectMapper.writeValueAsString(jwtAuthenticationResponse);
        
        // Act - Deserialize
        JwtAuthenticationResponse deserializedResponse = 
            objectMapper.readValue(json, JwtAuthenticationResponse.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserializedResponse);
        assertNull(deserializedResponse.getToken());
        assertNull(deserializedResponse.getUser());
    }

    @Test
    void jsonDeserialization_ShouldCreateObject_FromValidJson() throws Exception {
        // Arrange
        String json = "{\"token\":\"" + TEST_TOKEN + "\",\"user\":{\"id\":1,\"email\":\"test@example.com\"}}";

        // Act
        JwtAuthenticationResponse deserializedResponse = 
            objectMapper.readValue(json, JwtAuthenticationResponse.class);

        // Assert
        assertNotNull(deserializedResponse);
        assertEquals(TEST_TOKEN, deserializedResponse.getToken());
        assertNotNull(deserializedResponse.getUser());
        assertEquals(1L, deserializedResponse.getUser().getId());
        assertEquals("test@example.com", deserializedResponse.getUser().getEmail());
    }

    @Test
    void javaSerialization_ShouldSerializeAndDeserialize_Correctly() throws IOException, ClassNotFoundException {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);
        
        // Act - Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(jwtAuthenticationResponse);
        oos.close();
        
        // Act - Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        JwtAuthenticationResponse deserializedResponse = (JwtAuthenticationResponse) ois.readObject();
        ois.close();

        // Assert
        assertNotNull(deserializedResponse);
        assertEquals(jwtAuthenticationResponse.getToken(), deserializedResponse.getToken());
        assertEquals(jwtAuthenticationResponse.getUser().getId(), deserializedResponse.getUser().getId());
        assertEquals(jwtAuthenticationResponse.getUser().getEmail(), deserializedResponse.getUser().getEmail());
    }

    @Test
    void immutabilityTest_ShouldAllowModification_AfterCreation() {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);
        String originalToken = jwtAuthenticationResponse.getToken();
        User originalUser = jwtAuthenticationResponse.getUser();

        User updatedUser = new User();
        updatedUser.setId(2L);
        updatedUser.setEmail("updated@example.com");

        // Act
        jwtAuthenticationResponse.setToken(UPDATED_TOKEN);
        jwtAuthenticationResponse.setUser(updatedUser);

        // Assert
        assertNotEquals(originalToken, jwtAuthenticationResponse.getToken());
        assertNotEquals(originalUser, jwtAuthenticationResponse.getUser());
        assertEquals(UPDATED_TOKEN, jwtAuthenticationResponse.getToken());
        assertEquals(updatedUser, jwtAuthenticationResponse.getUser());
    }

    @Test
    void dataTransferObject_ShouldFunction_AsExpected() {
        // Arrange & Act
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        jwtAuthenticationResponse = new JwtAuthenticationResponse(TEST_TOKEN, testUser);

        // Assert - Verify it functions as a proper DTO
        assertNotNull(jwtAuthenticationResponse);
        assertEquals(TEST_TOKEN, jwtAuthenticationResponse.getToken());
        assertEquals(testUser, jwtAuthenticationResponse.getUser());
        
        // Verify mutability
        jwtAuthenticationResponse.setToken(UPDATED_TOKEN);
        assertEquals(UPDATED_TOKEN, jwtAuthenticationResponse.getToken());
        
        User updatedUser = new User();
        updatedUser.setId(2L);
        updatedUser.setEmail("updated@example.com");
        jwtAuthenticationResponse.setUser(updatedUser);
        assertEquals(updatedUser, jwtAuthenticationResponse.getUser());
    }
}