#!/usr/bin/env python3
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap


def generate_grid(width, height):
    max_lanes = 4
    lanes = [2, 4]
    min_block_size = 5
    grid = np.ones((height, width), dtype=int)

    for y in range(0, height, min_block_size):
        # lanelines = random.randint(1, max_lanes)
        # lanelines = random.choice(lanes)
        lanelines = 2
        grid[y:y + lanelines, :] = 0

    for x in range(0, width, min_block_size):
        # lanelines = random.randint(1, max_lanes)
        # lanelines = random.choice(lanes)
        lanelines = 2
        grid[:, x:x + lanelines] = 0

    return grid


# TODO: assign whole block as a type
# TODO: need better/random building assignment
def flood_fill(grid, x, y, new_val):
    target_val = grid[y, x]
    if target_val != 1:
        return []
    coords = []
    stack = [(x, y)]
    while stack:
        cx, cy = stack.pop()
        if grid[cy, cx] == target_val:
            grid[cy, cx] = new_val
            coords.append((cy, cx))
            if cx > 0:
                stack.append((cx - 1, cy))
            if cx < grid.shape[1] - 1:
                stack.append((cx + 1, cy))
            if cy > 0:
                stack.append((cx, cy - 1))
            if cy < grid.shape[0] - 1:
                stack.append((cx, cy + 1))
    return coords


def assign_roles(grid, num_hospitals, num_police_stations, num_fire_departments):
    block_id = 2  # Start IDs from 2 to avoid confusion with road (0) and building (1)
    blocks = []

    for y in range(grid.shape[0]):
        for x in range(grid.shape[1]):
            if grid[y, x] == 1:
                block = flood_fill(grid, x, y, block_id)
                if block:
                    blocks.append(block)
                    block_id += 1

    roles = [2] * num_hospitals + [3] * num_fire_departments + [4] * num_police_stations
    np.random.shuffle(roles)
    np.random.shuffle(blocks)

    for i, role in enumerate(roles):
        if i < len(blocks):
            for (y, x) in blocks[i]:
                grid[y, x] = role

    for y in range(grid.shape[0]):
        for x in range(grid.shape[1]):
            if grid[y, x] > 4:
                grid[y, x] = 1

    return grid


def find_intersections(grid):
    height, width = grid.shape
    # grid[0, 0] = -1
    for y in range(2, height - 2):
        for x in range(2, width - 2):
            if grid[y, x] > 0:
                if grid[y - 1, x - 1] == 0 and grid[y - 1, x] == 0 and grid[y, x - 1] == 0:
                    grid[y - 1, x - 1] = -1
                    i = 2
                    while y - i >= 0 and x - i >= 0 and grid[y - i, x - i] == 0:
                        if grid[y - i, x - i] == 0:
                            grid[y - i, x - i] = -1
                            grid[y - i, x - i + 1] = -1
                            grid[y - i + 1, x - i] = -1
                        else:
                            break
                        i += 1
                if grid[y + 1, x + 1] == 0 and grid[y + 1, x] == 0 and grid[y, x + 1] == 0:
                    grid[y + 1, x + 1] = -1
                    i = 2
                    while y + i < height and x + i < width and grid[y + i, x + i] == 0:
                        if grid[y + i, x + i] == 0:
                            grid[y + i, x + i] = -1
                            grid[y + i, x + i - 1] = -1
                            grid[y + i - 1, x + i] = -1
                        else:
                            break
                        i += 1
                if grid[y - 1, x + 1] == 0 and grid[y - 1, x] == 0 and grid[y, x + 1] == 0:
                    grid[y - 1, x + 1] = -1
                    i = 2
                    while y - i >= 0 and x + i < width and grid[y - i, x + i] == 0:
                        if grid[y - i, x + i] == 0:
                            grid[y - i, x + i] = -1
                            grid[y - i, x + i - 1] = -1
                            grid[y - i + 1, x + i] = -1
                        else:
                            break
                        i += 1
                if grid[y + 1, x - 1] == 0 and grid[y + 1, x] == 0 and grid[y, x - 1] == 0:
                    grid[y + 1, x - 1] = -1
                    i = 2
                    while y + i < height and x - i >= 0 and grid[y + i, x - i] == 0:
                        if grid[y + i, x - i] == 0:
                            grid[y + i, x - i] = -1
                            grid[y + i, x - i + 1] = -1
                            grid[y + i - 1, x - i] = -1
                        else:
                            break
                        i += 1
    return grid


def find_side_lanes(grid):
    height, width = grid.shape
    # grid[0, 1:width - 1] = grid[height - 1, 1:width - 1] = grid[1:height - 1, 0] = grid[1:height - 1, width - 1] = -2
    grid[0, grid[0] == 0] = -3
    grid[height - 1, grid[height - 1] == 0] = -2
    grid[grid[:, 0] == 0, 0] = -3
    grid[grid[:, width - 1] == 0, width - 1] = -2
    for y in range(height):
        for x in range(width):
            if grid[y, x] == 0:
                if x > 0 and grid[y, x - 1] >= 1:
                    grid[y, x] = -3  # Mark as left side lane
                elif x < width - 1 and grid[y, x + 1] >= 1:
                    grid[y, x] = -2  # Mark as right side lane
                elif y > 0 and grid[y - 1, x] >= 1:
                    grid[y, x] = -3
                elif y < height - 1 and grid[y + 1, x] >= 1:
                    grid[y, x] = -2
    return grid


def export_grid(grid, filename):
    with open(filename, 'w') as f:
        for row in grid:
            f.write(' '.join(map(str, row)) + '\n')
    print("Grid World exported at:", filename)


def visualize_grid(grid):
    cmap = ListedColormap(['purple', 'pink', 'orange', 'black', 'lightgray', 'white', 'red', 'blue'])
    # 0 - Tiles.Roads, 1 - General Buildings, 2 - Hospitals, 3 - Fire Departments, 4 - Police Stations

    fig, ax = plt.subplots()
    cax = ax.matshow(grid, cmap=cmap, interpolation='nearest')
    fig.colorbar(cax)

    ax.set_xticks(np.arange(-.5, len(grid[0]), 1), minor=True)
    ax.set_yticks(np.arange(-.5, len(grid), 1), minor=True)
    ax.grid(which='minor', color='gray', linestyle='-', linewidth=0.5)

    for y in range(len(grid)):
        for x in range(len(grid[y])):
            if grid[y, x] > 1 or grid[y, x] < 0:  # Only annotate special buildings
                ax.text(x, y,
                        'H' if grid[y, x] == 2 else
                        'F' if grid[y, x] == 3 else
                        'P' if grid[y, x] == 4 else
                        'I' if grid[y, x] == -1 else
                        'R' if grid[y, x] == -2 else 'L',
                        ha='center', va='center', color='black')

    plt.title('GridWorld Visualization')
    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    # Define grid dimensions and number of buildings
    width, height = 5 * 4 + 2, 5 * 4 + 2
    num_hospitals = 3
    num_police_stations = 1
    num_fire_departments = 2

    # Generate the grid
    grid = generate_grid(width, height)

    # Assign roles to buildings
    grid = assign_roles(grid, num_hospitals, num_police_stations, num_fire_departments)

    # Find and mark intersections
    grid = find_intersections(grid)

    # Find and mark side lanes
    grid = find_side_lanes(grid)

    print(grid)
    print(grid.shape)

    export_grid(grid, "grid_world.txt")

    # Plot the grid
    visualize_grid(grid)
