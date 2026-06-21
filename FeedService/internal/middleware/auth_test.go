// internal/middleware/auth_test.go
package middleware

import (
	"context"
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
)

type mockTokenValidator struct {
	mock.Mock
}

func (m *mockTokenValidator) ValidateToken(ctx context.Context, token string) (*dto.ValidateTokenResponse, error) {
	args := m.Called(ctx, token)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*dto.ValidateTokenResponse), args.Error(1)
}

func setupAuthTestContext(authHeader string) (*gin.Context, *httptest.ResponseRecorder) {
	gin.SetMode(gin.TestMode)
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)

	req := httptest.NewRequest(http.MethodGet, "/feed/user-1", nil)
	if authHeader != "" {
		req.Header.Set("Authorization", authHeader)
	}
	c.Request = req

	return c, w
}

func TestAuthRequired_NoHeader_ReturnsUnauthorized(t *testing.T) {
	validator := &mockTokenValidator{}
	c, w := setupAuthTestContext("")

	AuthRequired(validator)(c)

	require.Equal(t, http.StatusUnauthorized, w.Code)
	require.True(t, c.IsAborted())
}

func TestAuthRequired_MalformedHeader_ReturnsUnauthorized(t *testing.T) {
	validator := &mockTokenValidator{}
	c, w := setupAuthTestContext("Token abc123")

	AuthRequired(validator)(c)

	require.Equal(t, http.StatusUnauthorized, w.Code)
}

func TestAuthRequired_InvalidToken_ReturnsUnauthorized(t *testing.T) {
	validator := &mockTokenValidator{}
	validator.On("ValidateToken", mock.Anything, "bad-token").
		Return(nil, errors.New("token inválido"))

	c, w := setupAuthTestContext("Bearer bad-token")

	AuthRequired(validator)(c)

	require.Equal(t, http.StatusUnauthorized, w.Code)
	require.True(t, c.IsAborted())
}

func TestAuthRequired_ValidToken_SetsContextAndContinues(t *testing.T) {
	validator := &mockTokenValidator{}
	validator.On("ValidateToken", mock.Anything, "good-token").
		Return(&dto.ValidateTokenResponse{UserID: "user-1", Username: "Victor"}, nil)

	c, w := setupAuthTestContext("Bearer good-token")

	AuthRequired(validator)(c)

	require.False(t, c.IsAborted())
	require.Equal(t, "user-1", c.GetString("userId"))
	require.Equal(t, "Victor", c.GetString("username"))
	require.Equal(t, 200, w.Code)
}
