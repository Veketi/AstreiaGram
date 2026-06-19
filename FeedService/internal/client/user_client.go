package client

import (
	"context"
	"net/http"
)

type UserClient struct {
	baseUrl string
	httpClient *http.Client
}

func (c *UserClient) GetFollowers(
	ctx context.Context,
	userID string,
) ([]string, error) {
	//TODO:
	//GET /users/{id}/followers

	return nil, nil
}

