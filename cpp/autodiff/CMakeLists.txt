cmake_minimum_required(VERSION 2.8.3)
project(autodiff)

## Use c++ 11x std
set(CMAKE_CXX_FLAGS "-std=c++11 ${CMAKE_CXX_FLAGS}")
if (${CMAKE_EXTRA_GENERATOR} MATCHES "Eclipse CDT4")
	set(CMAKE_CXX_COMPILER_ARG1 "-std=c++11" CACHE STRING "C++ version for eclipse" FORCE)
	set(CMAKE_ECLIPSE_VERSION "4.5" CACHE STRING "Eclipse version" FORCE)
	add_definitions (-DCMAKE_ECLIPSE_GENERATE_SOURCE_PROJECT=TRUE)
endif (${CMAKE_EXTRA_GENERATOR} MATCHES "Eclipse CDT4")


## Find catkin macros and libraries
## if COMPONENTS list like find_package(catkin REQUIRED COMPONENTS xyz)
## is used, also find other catkin packages
find_package(catkin REQUIRED COMPONENTS
 fsystem
 alica_engine
)

## System dependencies are found with CMake's conventions
#find_package(Boost REQUIRED COMPONENTS system filesystem)


## Uncomment this if the package has a setup.py. This macro ensures
## modules and global scripts declared therein get installed
## See http://ros.org/doc/api/catkin/html/user_guide/setup_dot_py.html
# catkin_python_setup()

################################################
## Declare ROS messages, services and actions ##
################################################

## To declare and build messages, services or actions from within this
## package, follow these steps:
## * Let MSG_DEP_SET be the set of packages whose message types you use in
##   your messages/services/actions (e.g. std_msgs, actionlib_msgs, ...).
## * In the file package.xml:
##   * add a build_depend and a run_depend tag for each package in MSG_DEP_SET
##   * If MSG_DEP_SET isn't empty the following dependencies might have been
##     pulled in transitively but can be declared for certainty nonetheless:
##     * add a build_depend tag for "message_generation"
##     * add a run_depend tag for "message_runtime"
## * In this file (CMakeLists.txt):
##   * add "message_generation" and every package in MSG_DEP_SET to
##     find_package(catkin REQUIRED COMPONENTS ...)
##   * add "message_runtime" and every package in MSG_DEP_SET to
##     catkin_package(CATKIN_DEPENDS ...)
##   * uncomment the add_*_files sections below as needed
##     and list every .msg/.srv/.action file to be processed
##   * uncomment the generate_messages entry below
##   * add every package in MSG_DEP_SET to generate_messages(DEPENDENCIES ...)

## Generate messages in the 'msg' folder
# add_message_files(
#   FILES
#   Message1.msg
#   Message2.msg
# )

## Generate services in the 'srv' folder
# add_service_files(
#   FILES
#   Service1.srv
#   Service2.srv
# )

## Generate actions in the 'action' folder
# add_action_files(
#   FILES
#   Action1.action
#   Action2.action
# )

## Generate added messages and services with any dependencies listed here
# generate_messages(
#   DEPENDENCIES
#   std_msgs  # Or other packages containing msgs
# )

###################################
## catkin specific configuration ##
###################################
## The catkin_package macro generates cmake config files for your package
## Declare things to be passed to dependent projects
## INCLUDE_DIRS: uncomment this if you package contains header files
## LIBRARIES: libraries you create in this project that dependent projects also need
## CATKIN_DEPENDS: catkin_packages dependent projects also need
## DEPENDS: system dependencies of this project that dependent projects also need
catkin_package(
 INCLUDE_DIRS include
 LIBRARIES autodiff alica_engine
 CATKIN_DEPENDS fsystem
# DEPENDS system filesystem
)

###########
## Build ##
###########

## Specify additional locations of header files
## Your package locations should be listed before other locations
include_directories(include ${catkin_INCLUDE_DIRS})

## Declare a cpp library
add_library(autodiff
  src/CompiledDifferentiator.cpp
  src/Abs.cpp
  src/And.cpp
  src/Atan2.cpp
  src/Constant.cpp
  src/ConstPower.cpp
  src/ConstraintBuilder.cpp
  src/ConstraintUtility.cpp
  src/Cos.cpp
  src/Exp.cpp
  src/Gp.cpp
  src/LinSigmoid.cpp
  src/Log.cpp
  src/LTConstraint.cpp
  src/LTEConstraint.cpp
  src/Max.cpp
  src/Min.cpp
  src/Or.cpp
  src/Product.cpp
  src/Reification.cpp
  src/Sigmoid.cpp
  src/Sin.cpp
  src/Sum.cpp
  src/Term.cpp
  src/TermBuilder.cpp
  src/TermPower.cpp
  src/TermUtils.cpp
  src/TVec.cpp
  src/Variable.cpp
  src/Zero.cpp
  src/compiled/CompiledAbs.cpp
  src/compiled/CompiledAnd.cpp
  src/compiled/CompiledAtan2.cpp
  src/compiled/CompiledConstant.cpp
  src/compiled/CompiledConstPower.cpp
  src/compiled/CompiledConstraintUtility.cpp
  src/compiled/CompiledCos.cpp
  src/compiled/CompiledExp.cpp
  src/compiled/CompiledGp.cpp
  src/compiled/CompiledLinSigmoid.cpp
  src/compiled/CompiledLog.cpp
  src/compiled/CompiledLTConstraint.cpp
  src/compiled/CompiledLTEConstraint.cpp
  src/compiled/CompiledMax.cpp
  src/compiled/CompiledMin.cpp
  src/compiled/CompiledOr.cpp
  src/compiled/CompiledProduct.cpp
  src/compiled/CompiledReification.cpp
  src/compiled/CompiledSigmoid.cpp
  src/compiled/CompiledSin.cpp
  src/compiled/CompiledSum.cpp
  src/compiled/CompiledTermPower.cpp
  src/compiled/CompiledVariable.cpp
)

## Declare a cpp executable
# add_executable(autodiff_node src/autodiff_node.cpp)

## Add cmake target dependencies of the executable/library
## as an example, message headers may need to be generated before nodes
add_dependencies(autodiff ${catkin_LIBRARIES})

## Specify libraries to link a library or executable target against
target_link_libraries(autodiff ${catkin_LIBRARIES})

#############
## Install ##
#############

# all install targets should use catkin DESTINATION variables
# See http://ros.org/doc/api/catkin/html/adv_user_guide/variables.html

## Mark executable scripts (Python etc.) for installation
## in contrast to setup.py, you can choose the destination
# install(PROGRAMS
#   scripts/my_python_script
#   DESTINATION ${CATKIN_PACKAGE_BIN_DESTINATION}
# )

## Mark executables and/or libraries for installation
# install(TARGETS autodiff autodiff_node
#   ARCHIVE DESTINATION ${CATKIN_PACKAGE_LIB_DESTINATION}
#   LIBRARY DESTINATION ${CATKIN_PACKAGE_LIB_DESTINATION}
#   RUNTIME DESTINATION ${CATKIN_PACKAGE_BIN_DESTINATION}
# )

## Mark cpp header files for installation
# install(DIRECTORY include/${PROJECT_NAME}/
#   DESTINATION ${CATKIN_PACKAGE_INCLUDE_DESTINATION}
#   FILES_MATCHING PATTERN "*.h"
#   PATTERN ".svn" EXCLUDE
# )

## Mark other files for installation (e.g. launch and bag files, etc.)
# install(FILES
#   # myfile1
#   # myfile2
#   DESTINATION ${CATKIN_PACKAGE_SHARE_DESTINATION}
# )

#############
## Testing ##
#############

## Add gtest based cpp test target and link libraries
catkin_add_gtest(${PROJECT_NAME}-test test/test_autodiff.cpp)
if(TARGET ${PROJECT_NAME}-test)
  target_link_libraries(${PROJECT_NAME}-test ${PROJECT_NAME} ${catkin_LIBRARIES})
endif()

## Add folders to be run by python nosetests
# catkin_add_nosetests(test)
