var gulp = require('gulp');
var babel = require('gulp-babel');
var bower = require('gulp-bower');
var del = require('del');

// Write output to the Play public directory.
var target = '../../public/dist'
var paths = {
  scripts: 'es/**/*.es',
  scriptsTarget: target + '/js'
}

gulp.task('clean', function(cb) {
  del([
   paths.scriptsTarget
  ],
  { force: true },
  cb);
});

gulp.task('bower', function () {
  return bower();
})

gulp.task('default', function() {
  return gulp.src(paths.scripts)
    .pipe(babel())
    .pipe(gulp.dest(paths.scriptsTarget));
});

gulp.task('watch', function() {
  gulp.watch(paths.scripts, ['default']);
});
