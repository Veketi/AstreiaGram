package consumer

import (
	"context"

	"github.com/stretchr/testify/mock"
)

type mockFollowerService struct {
	mock.Mock
}

func (m *mockFollowerService) GetFollowers(ctx context.Context, userID string) ([]string, error) {
	args := m.Called(ctx, userID)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).([]string), args.Error(1)
}
