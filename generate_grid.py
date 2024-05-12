#!/usr/bin/env python3
import random
import numpy as np
import matplotlib.pyplot as plt

def generate_grid(width, height):
  max_lanes = 4
  min_block_size = 5
  grid = np.ones((height, width), dtype=int)

  for y in range(1, height, min_block_size):
    lanelines = random.randint(1, max_lanes)
    grid[y:y+lanelines, :] = 0

  for x in range(1, width, min_block_size):
    lanelines = random.randint(1, max_lanes)
    grid[:, x:x+lanelines] = 0

  return grid

# TODO: need better/random building assignment
def assign_roles(grid, num_hospitals, num_police_stations, num_fire_departments):
  # Get all coordinates of buildings
  building_coordinates = np.argwhere(grid == 1)

  # Count the number of buildings
  num_buildings = len(building_coordinates)

  # Randomly assign roles to buildings
  roles = [2] * num_hospitals + [3] * num_fire_departments + [4] * num_police_stations
  np.random.shuffle(roles)

  # Assign roles to buildings
  for i, (y, x) in enumerate(building_coordinates):
    if i < len(roles):  # Ensure roles list doesn't exceed building coordinates
      grid[y, x] = roles[i]

  return grid

def export_grid(grid, filename):
  with open(filename, 'w') as f:
    for row in grid:
      f.write(' '.join(map(str, row)) + '\n')
  print("Grid World exported at:", filename)


if __name__ == "__main__":
  # Define grid dimensions and number of buildings
  width, height = 90, 90
  num_hospitals = 3
  num_police_stations = 1
  num_fire_departments = 2

  # Generate the grid
  grid = generate_grid(width, height)

  # Assign roles to buildings
  grid = assign_roles(grid, num_hospitals, num_police_stations, num_fire_departments)

  print(grid)
  print(grid.shape)

  export_grid(grid, "grid_world.txt")

  # Plot the grid
  plt.imshow(grid, cmap='viridis', interpolation='nearest')
  plt.title('GridWorld')
  plt.axis('on')
  plt.grid(True)
  plt.show()
