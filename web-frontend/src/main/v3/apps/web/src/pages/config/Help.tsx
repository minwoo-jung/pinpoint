import { HelpPage, withInitialFetch } from '@pinpoint-fe/ui'; // TODO : (fe_minwoo) @pinpoint-fe/ui 는 파일 어떻게 찾아가야하나?

// withInitialFetch:
//     withInitialFetch는 함수 또는 고차 컴포넌트(Higher-Order Component, HOC)로 보입니다.
//     이 함수는 컴포넌트를 감싸서 초기 데이터를 가져오는(fetch) 로직을 추가하거나, 컴포넌트가 렌더링되기 전에 필요한 데이터를 미리 준비하는 역할을 할 가능성이 높습니다.
//     이 함수는 내부에 또 다른 함수를 인자로 받고 있습니다.
import {
  getLayoutWithConfiguration,
//   getLayoutWithConfiguration:
//    이 함수는 또 다른 레이아웃 설정 함수로 보입니다.
//    "Configuration(구성)"과 관련된 레이아웃을 추가적으로 설정하는 역할을 합니다.
//    getLayoutWithSideNavigation 안에 중첩되어 호출되므로, "사이드 내비게이션"과 "구성"이 결합된 레이아웃을 반환합니다.
  getLayoutWithSideNavigation,
//   getLayoutWithConfiguration:
//       이 함수는 또 다른 레이아웃 설정 함수로 보입니다.
//      "Configuration(구성)"과 관련된 레이아웃을 추가적으로 설정하는 역할을 합니다.
//       getLayoutWithSideNavigation 안에 중첩되어 호출되므로, "사이드 내비게이션"과 "구성"이 결합된 레이아웃을 반환합니다
} from '@pinpoint-fe/web/src/components/Layout';

export default withInitialFetch(() =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<HelpPage />)),
    // todo : (minwoo) withInitialFetch getLayoutWithSideNavigation getLayoutWithConfiguration 은 뭘까?
    // todo : (minwoo) f4 버튼 눌러서 바로가기 같은건 없는건가?
);
