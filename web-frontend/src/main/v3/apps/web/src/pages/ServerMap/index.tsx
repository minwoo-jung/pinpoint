import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';
import { withInitialFetch, ServerMapPage } from '@pinpoint-fe/ui';
// withInitialFetch configuration 미리 불어오기
export default withInitialFetch((props) =>
        // 사이드바 그려주기
        getLayoutWithSideNavigation(<ServerMapPage {...props} />),
    // ServerMapPage 서버맵그려주기
);