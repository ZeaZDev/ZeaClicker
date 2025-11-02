cmake_minimum_required(VERSION 3.10.2)
project("greenclicker_native")

add_library( # Specifies the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             native-lib.cpp )

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_compile_options(native-lib PRIVATE -O3 -march=native)
target_link_libraries( # Specifies the target library.
                       native-lib
                       ${log-lib} )
