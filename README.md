# mad camp 2nd week



# 팀원 이름

- 서유림
- 김상윤



# 프로젝트 이름

- Movie Diary
  - 영화 사진과 함께 리뷰를 써서 기록하는 영화 리뷰 다이어리
  - 나와 친구를 맺은 사람의 리뷰도 함께 볼 수 있고 서로 좋아요도 누를 수 있도록 함



# 구현 방식

##### 0) 전체적인 구조

- node.js에서 REST API 방식을 적용해서 mongoDB 접근 : 로그인 / 전화번호부 /  친구들 영화 리뷰 보기 탭
- node.js에서 Retrofit API 방식을 적용해서 mongoDB 접근 : 영화 리뷰 탭 / 친구들 영화 리뷰 보기 탭
- 주소록/영화 사진/친구 피드 탭 모두 RecyclerView 사용
- mongoDB 내부 collection 종류
  - users
    - my_email 필드 : primary key 기능
    - my_name, my_phone_num, my_pwd 필드 
  - address_book
    - my_email 필드 : primary key 기능
    - friend_email, friend_phone_num, friend_name 필드
  - images
    - my_email 필드 : primary key 기능
    - likes, title, rate, review 필드

##### 1) 로그인 탭	

- 회원가입 기능

  - 문자 인증 : smsManager 사용, 앱에서 생성한 난수를 입력한 전화번호로 문자메세지를 보냄
  - 입력받은 회원 이름, 이메일, 전화번호를 mongoDB에 저장

- 로그인 기능 

  - mongoDB에 저장된 user collection에 검색

- 페이스북 로그인 기능

  - Facebook SDK를 활용

    > 세 기능 모두, "my_email" 이라는 필드를 primary key로 활용하는 user collection에 접근하고 있기 때문에, 반드시 로그인 했을때의 이메일을 어플리케이션 실행 내내 유지할 수 있어야 한다
    >
    > ​	> sharedPreference 사용	

##### 2) 전화번호부 탭

- 친구 삭제, 추가, 검색 기능 : mongoDB 와 연동, address_book collection
  - AlertDialog 사용

##### 3) 영화 리뷰 탭 (=사진첩)

- 영화 관련 사진 추가, 삭제 기능
  - 영화 사진 추가시, 제목/후기/평점 작성 가능 : AlertDialog 적용
- 사진 클릭 시, 확대 / 슬라이드 기능

##### 4) 친구들의 영화 리뷰 보기 탭

- 전체적인 순서 
  - address_book collection에 접근해서, 현재 내 이메일과 연결된 친구 이메일들을 받아온다
  - 친구 이메일들 하나하나 모두 images 테이블에 접근해서, 사진들의 주소를 모두 받아온다
- 기능
  - 내 친구들이 올린 영화 리뷰들을 모두 볼 수 있다
    - 제목, 평점, 후기, 좋아요 갯수, 사진을 확인 가능
  - 친구들의 영화 피드에 있는 "?" 버튼 클릭 시, 영화에 대한 제목, 설명, 평점이 올라옴
    - 네이버 영화 API 사용 : REST API 방식
  - 좋아요 버튼 클릭 시, 해당 영화 사진 document에 대응되는 "likes" 필드가 업데이트 됨 (=좋아요 필드 업데이트)
    - mongoDB 쿼리 중, update 쿼리 사용

##### 5) 현재 로그인 상태/게시물 수 표시 레이아웃

- 적용 방식 : Navigation Bar 

- 기능 : 

  - facebook 로그아웃 또는 어플 계정 로그아웃 버튼 추가
  - 현재 내가 올린 게시물 수 보여주기

  

  
