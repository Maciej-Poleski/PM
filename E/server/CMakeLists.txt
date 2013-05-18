cmake_minimum_required(VERSION 2.8 FATAL_ERROR)
project(server)

set(CMAKE_CXX_FLAGS "-std=c++11")

find_package(Wt REQUIRED)
find_package(Boost REQUIRED signals system)
INCLUDE_DIRECTORIES( ${Wt_INCLUDE_DIR} )
include_directories(${Boost_INCLUDE_DIR})

add_executable(server main.cpp)
TARGET_LINK_LIBRARIES( server
    ${Wt_DEBUG_LIBRARY}        # or {Wt_LIBRARY}
    ${Wt_HTTP_DEBUG_LIBRARY}   # or {Wt_HTTP_LIBRARY}
    ${Boost_LIBRARIES}
  )

install(TARGETS server RUNTIME DESTINATION bin)
