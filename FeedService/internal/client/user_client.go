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
		return nil, err
	}

	resp, err := c.httpClient.Do(req)

	if err != nil {
		return nil, err
	}

	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("user-service returned %d", resp.StatusCode)
	}

	var response dto.UsersReponse

	if err := json.NewDecoder(resp.Body).Decode(&response); err != nil {
		return nil, err
	}

	followers := make([]string, 0, len(response.Users))

	for _, user := range response.Users {
		followers = append(followers, user.ID)
	}

	return followers, nil
}

