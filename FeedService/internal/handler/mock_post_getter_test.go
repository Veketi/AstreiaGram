package handler

import (
	"context"

	"github.com/Veketi/astreiagram/feed-service/internal/dto"
	"github.com/stretchr/testify/mock"
)

type mockPostGetter struct {
	mock.Mock
}

func (m *mockPostGetter) GetPosts(ctx context.Context, ids []string) ([]dto.PostResponse, error) {
	args := m.Called(ctx, ids)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]dto.PostResponse), args.Error(1)
}
