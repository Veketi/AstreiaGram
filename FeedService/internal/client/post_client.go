package client

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"net/url"
	"strings"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
)

type PostClient struct {
	baseUrl string
	httpClient *http.Client
}

func NewPostClient(baseUrl string) *PostClient {
	return &PostClient{
		baseUrl: baseUrl,
		httpClient: &http.Client{},
	}
}

func (c *PostClient) GetPosts(
    ctx context.Context,
    ids []string,
) ([]dto.PostResponse, error) {
	if len(ids) == 0 {
		return []dto.PostResponse{}, nil
	}

	idsParam := strings.Join(ids, ",")

	reqUrl := fmt.Sprintf("%s/api/posts?ids=%s", c.baseUrl, url.QueryEscape(idsParam))

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, reqUrl, nil)
	if err != nil {
		return nil, fmt.Errorf("erro ao criar request: %w", err)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("erro ao chamar post-service: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("post-service retornou status %d", resp.StatusCode)
	}

	var posts []dto.PostResponse
	if err := json.NewDecoder(resp.Body).Decode(&posts); err != nil {
		return nil, fmt.Errorf("erro ao decodificar resposta: %w", err)
	}

	return posts, nil
}
