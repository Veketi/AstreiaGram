package client

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
)

type UserClient struct {
	baseUrl string
	httpClient *http.Client
}

func NewUserClient(baseUrl string) *UserClient {
	return &UserClient{
		baseUrl: baseUrl,
		httpClient: &http.Client{},
	}
}

func (c *UserClient) GetFollowers(
	ctx context.Context,
	userID string,
) ([]string, error) {
	url :=  fmt.Sprintf(
		"%s/api/users/%s/followers",
		c.baseUrl,
		userID,
	)

	req, err := http.NewRequestWithContext(
		ctx,
		http.MethodGet,
		url,
		nil,
	)

	if err != nil {
		return nil, fmt.Errorf("error while creating the request at %s: %w", url, err)
	}

	resp, err := c.httpClient.Do(req)

	if err != nil {
		return nil, fmt.Errorf("error while calling the user-service at %s: %w", url, err)
	}

	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("user-service returned status %d for user %s", resp.StatusCode, userID)
	}

	var response dto.UsersReponse

	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		return nil, fmt.Errorf("error while decoding followers response: %w", err)
	}

	followers := make([]string, 0, len(response.Users))

	for _, user := range response.Users {
		followers = append(followers, user.ID)
	}

	return followers, nil
}

func (c *UserClient) ValidateToken(
	ctx context.Context, 
	token string,
) (*dto.ValidateTokenResponse, error) {
	url := fmt.Sprintf("%s/api/auth/validate", c.baseUrl)

	req, err := http.NewRequestWithContext(
		ctx, 
		http.MethodPost, 
		url,
		nil,
	)

	if err != nil {
		return nil, fmt.Errorf("error while creating the request at %s: %w", url, err)
	}

	req.Header.Set("Authorization", "Bearer " + token)

	resp, err := c.httpClient.Do(req)

	if err != nil {
		return nil, fmt.Errorf("error while calling user-service at %s: %w", url, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("invalid token: user-service returned status %d", resp.StatusCode)
	}

	var validation dto.ValidateTokenResponse

	if err := json.NewDecoder(resp.Body).Decode(&validation); err != nil {
		return nil, fmt.Errorf("error decoding validate token response: %w", err)
	}

	return &validation, nil
}
