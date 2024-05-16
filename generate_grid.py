#!/usr/bin/env python3
import random
import numpy as np
import matplotlib.pyplot as plt

def generate_grid(width, height):
  max_lanes = 4
  lanes = [2, 4]
  min_block_size = 5
  grid = np.ones((height, width), dtype=int)

  for y in range(0, height, min_block_size):
    # lanelines = random.randint(1, max_lanes)
    # lanelines = random.choice(lanes)
    lanelines = 2
    grid[y:y+lanelines, :] = 0

  for x in range(0, width, min_block_size):
    # lanelines = random.randint(1, max_lanes)
    # lanelines = random.choice(lanes)
    lanelines = 2
    grid[:, x:x+lanelines] = 0

  return grid

# TODO: assign whole block as a type
# TODO: need better/random building assignment
def assign_roles(grid, num_hospitals, num_police_stations, num_fire_departments):
  # Get all coordinates of buildings
  
  # FIXME: buildings are at least 5x5
  building_coordinates = np.argwhere(grid == 1)
  print(building_coordinates)

  # Count the number of buildings
  num_buildings = len(building_coordinates)

  # Randomly assign roles to buildings
  roles = [2] * num_hospitals + [3] * num_fire_departments + [4] * num_police_stations
  np.random.shuffle(roles)
  print(roles)

  # Assign roles to buildings
  #FIXME: randomly assign buildings (random select from building_coords)
  for i in range(len(roles)):
    pass

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
  width, height = 10, 10
  num_hospitals = 3
  num_police_stations = 1
  num_fire_departments = 2

  # Generate the grid
  grid = generate_grid(width, height)

  # Assign roles to buildings
  # grid = assign_roles(grid, num_hospitals, num_police_stations, num_fire_departments)

  print(grid)
  print(grid.shape)

  export_grid(grid, "grid_world.txt")

  # Plot the grid
  plt.imshow(grid, cmap='viridis', interpolation='nearest')
  plt.title('GridWorld')
  plt.axis('on')
  plt.grid(True)
  plt.show()
