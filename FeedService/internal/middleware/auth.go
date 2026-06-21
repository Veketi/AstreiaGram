package middleware

import (
	"log"
	"net/http"
	"strings"
	"context"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
	"github.com/gin-gonic/gin"
)

type TokenValidator interface {
	ValidateToken(ctx context.Context, token string) (*dto.ValidateTokenResponse, error)
}

func AuthRequired(userClient TokenValidator) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" || !strings.HasPrefix(authHeader, "Bearer ") {
			c.AbortWithStatusJSON(http.StatusUnauthorized, dto.ErrorResponse{Error: "Token Required"})
			return
		}

		token := strings.TrimPrefix(authHeader, "Bearer ")

		validation, err := userClient.ValidateToken(c.Request.Context(), token)
		if err != nil {
			log.Printf("erro ao validar token: %v", err)
			c.AbortWithStatusJSON(http.StatusUnauthorized, dto.ErrorResponse{Error: "Invalid Token"})
			return
		}

		c.Set("userId", validation.UserID)
		c.Set("username", validation.Username)
		c.Next()
	}
}
